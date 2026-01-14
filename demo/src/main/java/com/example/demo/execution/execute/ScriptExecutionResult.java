package com.example.demo.execution.execute;

public class ScriptExecutionResult {
	private final boolean success;
	private final int exitCode;
	private final String stdout;
	private final String stderr;
	private final String errorMessage;
	
	public ScriptExecutionResult(boolean success, int exitCode, String stdout, String stderr, String errorMessage) {
		this.success = success;
		this.exitCode = exitCode;
		this.stdout = stdout;
		this.stderr = stderr;
		this.errorMessage = errorMessage;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public int getExitCode() {
		return exitCode;
	}
	
	public String getStdout() {
		return stdout;
	}
	
	public String getStderr() {
		return stderr;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
