package com.example.demo.execution.service;

import com.example.demo.execution.dto.request.ExecuteRequest;
import com.example.demo.execution.executor.ExecutionSpec;
import com.example.demo.execution.executor.LanguageSpecFactory;
import com.example.demo.execution.model.ExecutionSession;
import com.example.demo.execution.model.ExecutionStatus;
import com.example.demo.execution.websocket.ExecutionWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Docker 컨테이너 실행 및 스트리밍 처리.
 */
@Service
@RequiredArgsConstructor
public class ExecutionService {
  // 실행 제한값 (무료 플랜 기준)
  private static final long TIMEOUT_SECONDS = 30;
  private static final long MEMORY_BYTES = 256L * 1024 * 1024;
  private static final long CPU_PERIOD = 100_000L;
  private static final long CPU_QUOTA = 50_000L;
  private static final long PIDS_LIMIT = 64L;

  private final DockerClient docker;
  private final LanguageSpecFactory specFactory;
  private final ExecutionSessionRegistry registry;
  private final ExecutionWebSocketHandler wsHandler;
  private final ObjectMapper objectMapper;
  private final Semaphore limiter = new Semaphore(3);
  private final ExecutorService executor = Executors.newCachedThreadPool();

  /**
   * 실행 요청을 등록하고 runId를 반환한다.
   */
  public String start(ExecuteRequest request) {
    String runId = UUID.randomUUID().toString();
    ExecutionSession session = new ExecutionSession(runId);
    registry.put(session);

    // 초기 상태를 클라이언트에 전달
    sendStatus(runId, ExecutionStatus.READY);
    CompletableFuture.runAsync(() -> execute(session, request), executor);
    return runId;
  }

  /**
   * WebSocket 입력을 컨테이너 stdin으로 전달한다.
   */
  public void sendInput(String runId, String input) {
    ExecutionSession session = registry.get(runId);
    if (session == null || session.getStdin() == null) {
      return;
    }
    try {
      OutputStream stdin = session.getStdin();
      stdin.write(input.getBytes(StandardCharsets.UTF_8));
      stdin.flush();
    } catch (IOException ignored) {
    }
  }

  /**
   * 실행 중단 요청 처리.
   */
  public void stop(String runId) {
    ExecutionSession session = registry.get(runId);
    if (session == null || session.getContainerId() == null) {
      return;
    }
    try {
      docker.killContainerCmd(session.getContainerId()).exec();
      session.setStatus(ExecutionStatus.STOPPED);
      sendStatus(runId, ExecutionStatus.STOPPED);
    } catch (Exception ignored) {
    }
  }

  // 컨테이너 생성/실행/스트리밍/정리를 한 번에 처리한다.
  private void execute(ExecutionSession session, ExecuteRequest request) {
    boolean acquired = false;
    Path tempDir = null;
    String containerId = null;
    ResultCallback.Adapter<Frame> logCallback = null;
    PipedInputStream stdinPipe = null;
    PipedOutputStream stdinWriter = null;
    try {
      limiter.acquire();
      acquired = true;

      // 언어에 맞는 이미지/커맨드 선택
      ExecutionSpec spec = specFactory.getSpec(request.getLanguage());
      tempDir = Files.createTempDirectory("run-" + session.getRunId());
      session.setTempDir(tempDir);

      // 소스 코드 파일 저장
      writeFile(tempDir.resolve(spec.getFileName()), request.getCode());

      // 컨테이너 자원/보안 제한 설정
      HostConfig hostConfig = HostConfig.newHostConfig()
              .withBinds(new Bind(tempDir.toString(), new Volume("/workspace")))
              .withReadonlyRootfs(true)
              .withNetworkMode("none")
              .withMemory(MEMORY_BYTES)
              .withCpuPeriod(CPU_PERIOD)
              .withCpuQuota(CPU_QUOTA)
              .withPidsLimit(PIDS_LIMIT);

      // 컨테이너 생성
      containerId = docker.createContainerCmd(spec.getImage())
              .withHostConfig(hostConfig)
              .withWorkingDir("/workspace")
              .withCmd("sh", "-c", spec.getCommand())
              .withAttachStdout(true)
              .withAttachStderr(true)
              .withOpenStdin(true)
              .exec()
              .getId();

      session.setContainerId(containerId);
      docker.startContainerCmd(containerId).exec();

      session.setStatus(ExecutionStatus.RUNNING);
      sendStatus(session.getRunId(), ExecutionStatus.RUNNING);

      // stdin 파이프 연결 (WebSocket 입력 전달용)
      stdinWriter = new PipedOutputStream();
      stdinPipe = new PipedInputStream(stdinWriter);
      session.setStdin(stdinWriter);

      // stdout/stderr 스트리밍
      logCallback = new ResultCallback.Adapter<>() {
        @Override
        public void onNext(Frame frame) {
          String payload = new String(frame.getPayload(), StandardCharsets.UTF_8);
          if (frame.getStreamType() == StreamType.STDERR) {
            sendEvent(session.getRunId(), "stderr", payload);
          } else {
            sendEvent(session.getRunId(), "stdout", payload);
          }
        }
      };

      docker.attachContainerCmd(containerId)
              .withStdOut(true)
              .withStdErr(true)
              .withFollowStream(true)
              .withStdIn(stdinPipe)
              .exec(logCallback);

      // 초기 입력 값이 있으면 바로 주입
      if (request.getInput() != null && !request.getInput().isBlank()) {
        sendInput(session.getRunId(), request.getInput());
      }

      // 종료 대기 (타임아웃 적용)
      CompletableFuture<Integer> waitFuture = CompletableFuture.supplyAsync(
              () -> docker.waitContainerCmd(containerId)
                      .exec(new WaitContainerResultCallback())
                      .awaitStatusCode(),
              executor
      );

      int exitCode = waitFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      sendEvent(session.getRunId(), "exit", String.valueOf(exitCode));
      if (session.getStatus() != ExecutionStatus.STOPPED) {
        sendStatus(session.getRunId(), ExecutionStatus.FINISHED);
      }
    } catch (TimeoutException e) {
      // 타임아웃 발생 시 강제 종료
      session.setStatus(ExecutionStatus.TIMEOUT);
      sendStatus(session.getRunId(), ExecutionStatus.TIMEOUT);
      if (containerId != null) {
        try {
          docker.killContainerCmd(containerId).exec();
        } catch (Exception ignored) {
        }
      }
    } catch (Exception e) {
      // 실행 중 예외 처리
      sendStatus(session.getRunId(), ExecutionStatus.ERROR);
      sendEvent(session.getRunId(), "error", e.getMessage());
    } finally {
      // 리소스 정리
      if (logCallback != null) {
        try {
          logCallback.close();
        } catch (IOException ignored) {
        }
      }
      if (stdinPipe != null) {
        try {
          stdinPipe.close();
        } catch (IOException ignored) {
        }
      }
      if (stdinWriter != null) {
        try {
          stdinWriter.close();
        } catch (IOException ignored) {
        }
      }
      if (containerId != null) {
        try {
          docker.removeContainerCmd(containerId).withForce(true).exec();
        } catch (Exception ignored) {
        }
      }
      if (tempDir != null) {
        deleteDir(tempDir);
      }
      if (acquired) {
        limiter.release();
      }
      registry.remove(session.getRunId());
      wsHandler.close(session.getRunId());
    }
  }

  // 상태 변경 이벤트 전송
  private void sendStatus(String runId, ExecutionStatus status) {
    sendEvent(runId, "status", status.name());
  }

  // WebSocket 메시지 전송
  private void sendEvent(String runId, String type, String data) {
    try {
      String payload = objectMapper.writeValueAsString(Map.of("type", type, "data", data));
      wsHandler.send(runId, payload);
    } catch (Exception ignored) {
    }
  }

  private void writeFile(Path path, String content) throws IOException {
    Files.writeString(path, content == null ? "" : content, StandardCharsets.UTF_8);
  }

  private void deleteDir(Path path) {
    try {
      Files.walk(path)
              .sorted((a, b) -> b.compareTo(a))
              .forEach(p -> {
                try {
                  Files.delete(p);
                } catch (Exception ignored) {
                }
              });
    } catch (Exception ignored) {
    }
  }
}
