package com.example.demo.execution.execute;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.example.demo.execution.model.LanguageType;

/**
 * Python 코드를 컨테이너에서 실행합니다.
 */
@Component
public class DockerPythonExecutor implements LanguageExecutor {
	private final DockerExecutionEngine engine;
	private final ExecutionProperties properties;

	public DockerPythonExecutor(DockerExecutionEngine engine, ExecutionProperties properties) {
		this.engine = engine;
		this.properties = properties;
	}

	@Override
	public LanguageType getLanguage() {
		return LanguageType.PYTHON;
	}

	@Override
	public ScriptExecutionResult execute(String code, String[] args, InputStream stdin, OutputStream stdout, OutputStream stderr,
			Consumer<Process> processConsumer) {
		if(code == null || code.trim().isEmpty()) {
			return new ScriptExecutionResult(false, -1, "", "", "code is required");
		}

		Map<String, String> files = new HashMap<>();
		files.put("main.py", code);

		DockerExecutionRequest request = new DockerExecutionRequest(
				properties.getDocker().getPythonImage(),
				files,
				Arrays.asList("python", "-u", "/workspace/main.py"),
				args,
				stdin,
				stdout,
				stderr,
				processConsumer
		);
		return engine.execute(request);
	}
}
