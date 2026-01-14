package com.example.demo.execution.execute;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Docker 컨테이너 실행 요청 정보.
 */
public class DockerExecutionRequest {
	private final String image;
	private final Map<String, String> files;
	private final List<String> command;
	private final String[] args;
	private final InputStream stdin;
	private final OutputStream stdout;
	private final OutputStream stderr;
	private final Consumer<Process> processConsumer;

	public DockerExecutionRequest(String image, Map<String, String> files, List<String> command, String[] args,
			InputStream stdin, OutputStream stdout, OutputStream stderr, Consumer<Process> processConsumer) {
		this.image = image;
		this.files = files;
		this.command = command;
		this.args = args;
		this.stdin = stdin;
		this.stdout = stdout;
		this.stderr = stderr;
		this.processConsumer = processConsumer;
	}

	public String getImage() {
		return image;
	}

	public Map<String, String> getFiles() {
		return files;
	}

	public List<String> getCommand() {
		return command;
	}

	public String[] getArgs() {
		return args;
	}

	public InputStream getStdin() {
		return stdin;
	}

	public OutputStream getStdout() {
		return stdout;
	}

	public OutputStream getStderr() {
		return stderr;
	}

	public Consumer<Process> getProcessConsumer() {
		return processConsumer;
	}
}
