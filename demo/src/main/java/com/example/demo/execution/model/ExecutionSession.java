package com.example.demo.execution.model;

import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Instant;

/**
 * 실행 세션 정보.
 */
public class ExecutionSession {
  // 실행 식별자
  private final String runId;
  // 실행 상태
  private volatile ExecutionStatus status;
  // Docker 컨테이너 ID
  private volatile String containerId;
  // 임시 작업 디렉토리
  private volatile Path tempDir;
  // 컨테이너 stdin 스트림
  private volatile OutputStream stdin;
  // 세션 생성 시각
  private final Instant createdAt = Instant.now();

  public ExecutionSession(String runId) {
    this.runId = runId;
    this.status = ExecutionStatus.READY;
  }

  public String getRunId() {
    return runId;
  }

  public ExecutionStatus getStatus() {
    return status;
  }

  public void setStatus(ExecutionStatus status) {
    this.status = status;
  }

  public String getContainerId() {
    return containerId;
  }

  public void setContainerId(String containerId) {
    this.containerId = containerId;
  }

  public Path getTempDir() {
    return tempDir;
  }

  public void setTempDir(Path tempDir) {
    this.tempDir = tempDir;
  }

  public OutputStream getStdin() {
    return stdin;
  }

  public void setStdin(OutputStream stdin) {
    this.stdin = stdin;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
