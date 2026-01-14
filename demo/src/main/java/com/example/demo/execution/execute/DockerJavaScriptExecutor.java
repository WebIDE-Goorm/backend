package com.example.demo.execution.execute;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.example.demo.execution.model.LanguageType;

/**
 * JavaScript 실행기 (현재 컴포넌트 미등록).
 */
public class DockerJavaScriptExecutor implements LanguageExecutor {
	private final DockerExecutionEngine engine;
	private final ExecutionProperties properties;

	public DockerJavaScriptExecutor(DockerExecutionEngine engine, ExecutionProperties properties) {
		this.engine = engine;
		this.properties = properties;
	}

	@Override
	public LanguageType getLanguage() {
		return LanguageType.JAVASCRIPT;
	}

	@Override
	public ScriptExecutionResult execute(String code, String[] args, InputStream stdin, OutputStream stdout, OutputStream stderr,
			Consumer<Process> processConsumer) {
		if(code == null || code.trim().isEmpty()) {
			return new ScriptExecutionResult(false, -1, "", "", "code is required");
		}

		Map<String, String> files = new HashMap<>();
		files.put("main.js", code);

		DockerExecutionRequest request = new DockerExecutionRequest(
				properties.getDocker().getJavascriptImage(),
				files,
				Arrays.asList("node", "/workspace/main.js"),
				args,
				stdin,
				stdout,
				stderr,
				processConsumer
		);
		return engine.execute(request);
	}
}
