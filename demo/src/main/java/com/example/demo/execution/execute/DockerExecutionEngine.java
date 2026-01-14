package com.example.demo.execution.execute;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Docker 컨테이너에서 코드를 실행하고 stdout/stderr/exitCode를 수집합니다.
 */
@Component
public class DockerExecutionEngine {
	private static final String DOCKER_COMMAND = "docker";
	private static final String CONTAINER_WORKDIR = "/workspace";
	private final ExecutionProperties properties;

	public DockerExecutionEngine(ExecutionProperties properties) {
		this.properties = properties;
	}

	public ScriptExecutionResult execute(DockerExecutionRequest request) {
		if(request == null) {
			return new ScriptExecutionResult(false, -1, "", "", "execution request is required");
		}
		if(request.getImage() == null || request.getImage().trim().isEmpty()) {
			return new ScriptExecutionResult(false, -1, "", "", "docker image is required");
		}
		if(request.getCommand() == null || request.getCommand().isEmpty()) {
			return new ScriptExecutionResult(false, -1, "", "", "container command is required");
		}

		Path workDir = null;
		ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
		ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();
		OutputStream stdoutTarget = tee(request.getStdout(), stdoutBuffer);
		OutputStream stderrTarget = tee(request.getStderr(), stderrBuffer);
		Process process = null;
		Thread stdoutThread = null;
		Thread stderrThread = null;
		Thread stdinThread = null;

		try {
			// 컨테이너에 마운트할 로컬 작업 디렉토리를 준비합니다.
			workDir = createWorkDir();
			writeFiles(workDir, request.getFiles());

			List<String> dockerCommand = buildDockerCommand(workDir, request);
			ProcessBuilder builder = new ProcessBuilder(dockerCommand);
			builder.directory(workDir.toFile());
			process = builder.start();

			if(request.getProcessConsumer() != null) {
				request.getProcessConsumer().accept(process);
			}

			stdoutThread = pump(process.getInputStream(), stdoutTarget, false);
			stderrThread = pump(process.getErrorStream(), stderrTarget, false);

			if(request.getStdin() != null) {
				stdinThread = pump(request.getStdin(), process.getOutputStream(), true);
			} else {
				process.getOutputStream().close();
			}

			process.waitFor();

			joinQuietly(stdoutThread);
			joinQuietly(stderrThread);
			joinQuietly(stdinThread, 200L);

			String outText = new String(stdoutBuffer.toByteArray(), StandardCharsets.UTF_8);
			String errText = new String(stderrBuffer.toByteArray(), StandardCharsets.UTF_8);
			int exitCode = process.exitValue();
			boolean success = exitCode == 0;
			String errorMessage = null;
			if(!success) {
				errorMessage = !errText.isEmpty() ? errText : "process exited with code " + exitCode;
			}
			return new ScriptExecutionResult(success, exitCode, outText, errText, errorMessage);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			if(process != null) {
				process.destroyForcibly();
			}
			String outText = new String(stdoutBuffer.toByteArray(), StandardCharsets.UTF_8);
			String errText = new String(stderrBuffer.toByteArray(), StandardCharsets.UTF_8);
			return new ScriptExecutionResult(false, -1, outText, errText, "execution interrupted");
		} catch (Exception e) {
			if(process != null) {
				process.destroyForcibly();
			}
			String outText = new String(stdoutBuffer.toByteArray(), StandardCharsets.UTF_8);
			String errText = new String(stderrBuffer.toByteArray(), StandardCharsets.UTF_8);
			return new ScriptExecutionResult(false, -1, outText, errText, e.getMessage());
		} finally {
			flushQuietly(stdoutTarget);
			flushQuietly(stderrTarget);
			closeQuietly(request.getStdin());
			deleteQuietly(workDir);
		}
	}

	private Path createWorkDir() throws IOException {
		if(properties == null) {
			return Files.createTempDirectory("compiler-exec-");
		}
		String baseDir = properties.getWorkDir();
		if(baseDir == null || baseDir.trim().isEmpty()) {
			return Files.createTempDirectory("compiler-exec-");
		}
		Path basePath = Paths.get(baseDir);
		Files.createDirectories(basePath);
		return Files.createTempDirectory(basePath, "compiler-exec-");
	}

	private static void writeFiles(Path workDir, Map<String, String> files) throws IOException {
		if(files == null || files.isEmpty()) {
			return;
		}
		for(Map.Entry<String, String> entry : files.entrySet()) {
			Path filePath = workDir.resolve(entry.getKey());
			Path parent = filePath.getParent();
			if(parent != null) {
				Files.createDirectories(parent);
			}
			Files.write(filePath, entry.getValue().getBytes(StandardCharsets.UTF_8));
		}
	}

	private static List<String> buildDockerCommand(Path workDir, DockerExecutionRequest request) {
		List<String> command = new ArrayList<>();
		command.add(DOCKER_COMMAND);
		command.add("run");
		command.add("--rm");
		command.add("-i");
		// 외부 네트워크 접근을 차단합니다.
		command.add("--network");
		command.add("none");
		// 로컬 작업 디렉토리를 컨테이너 작업 디렉토리로 마운트합니다.
		command.add("-v");
		command.add(workDir.toAbsolutePath().toString() + ":" + CONTAINER_WORKDIR);
		command.add("-w");
		command.add(CONTAINER_WORKDIR);
		command.add(request.getImage());
		command.addAll(request.getCommand());
		if(request.getArgs() != null) {
			for(String arg : request.getArgs()) {
				command.add(arg);
			}
		}
		return command;
	}

	private static Thread pump(InputStream input, OutputStream output, boolean closeOutput) {
		Thread thread = new Thread(() -> {
			byte[] buffer = new byte[4096];
			int len;
			try {
				while((len = input.read(buffer)) != -1) {
					output.write(buffer, 0, len);
					output.flush();
				}
			} catch (IOException e) {
				// ignore
			} finally {
				closeQuietly(input);
				if(closeOutput) {
					closeQuietly(output);
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
		return thread;
	}

	private static void joinQuietly(Thread thread) {
		joinQuietly(thread, 0L);
	}

	private static void joinQuietly(Thread thread, long timeoutMillis) {
		if(thread == null) {
			return;
		}
		try {
			if(timeoutMillis > 0) {
				thread.join(timeoutMillis);
			} else {
				thread.join();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static void flushQuietly(OutputStream output) {
		if(output == null) {
			return;
		}
		try {
			output.flush();
		} catch (IOException e) {
			// ignore
		}
	}

	private static void closeQuietly(InputStream input) {
		if(input == null) {
			return;
		}
		try {
			input.close();
		} catch (IOException e) {
			// ignore
		}
	}

	private static void closeQuietly(OutputStream output) {
		if(output == null) {
			return;
		}
		try {
			output.close();
		} catch (IOException e) {
			// ignore
		}
	}

	private static void deleteQuietly(Path path) {
		if(path == null) {
			return;
		}
		try {
			if(!Files.exists(path)) {
				return;
			}
			Files.walk(path)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		} catch (IOException e) {
			// ignore
		}
	}

	private static OutputStream tee(OutputStream primary, OutputStream secondary) {
		if(primary == null) {
			return secondary;
		}
		if(secondary == null) {
			return primary;
		}
		return new MultiOutputStream(primary, secondary);
	}

	private static class MultiOutputStream extends OutputStream {
		private final OutputStream left;
		private final OutputStream right;

		MultiOutputStream(OutputStream left, OutputStream right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public void write(int b) throws IOException {
			left.write(b);
			right.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			left.write(b, off, len);
			right.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			left.flush();
			right.flush();
		}
	}
}
