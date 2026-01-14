package com.example.demo.execution.execute;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.example.demo.execution.model.LanguageType;

/**
 * Java 소스를 컨테이너에서 컴파일 후 실행합니다.
 */
@Component
public class DockerJavaExecutor implements LanguageExecutor {
	private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("(?m)^\\s*(public\\s+)?class\\s+([A-Za-z_][A-Za-z0-9_]*)");

	private final DockerExecutionEngine engine;
	private final ExecutionProperties properties;

	public DockerJavaExecutor(DockerExecutionEngine engine, ExecutionProperties properties) {
		this.engine = engine;
		this.properties = properties;
	}

	@Override
	public LanguageType getLanguage() {
		return LanguageType.JAVA;
	}

	@Override
	public ScriptExecutionResult execute(String code, String[] args, InputStream stdin, OutputStream stdout, OutputStream stderr,
			Consumer<Process> processConsumer) {
		if(code == null || code.trim().isEmpty()) {
			return new ScriptExecutionResult(false, -1, "", "", "code is required");
		}

		String className = extractClassName(code);
		if(className == null) {
			return new ScriptExecutionResult(false, -1, "", "", "class name is required");
		}

		Map<String, String> files = new HashMap<>();
		files.put(className + ".java", code);
		files.put("run.sh", buildRunScript(className));

		DockerExecutionRequest request = new DockerExecutionRequest(
				properties.getDocker().getJavaImage(),
				files,
				Arrays.asList("sh", "/workspace/run.sh"),
				args,
				stdin,
				stdout,
				stderr,
				processConsumer
		);
		return engine.execute(request);
	}

	private static String extractClassName(String code) {
		Matcher matcher = CLASS_NAME_PATTERN.matcher(code);
		if(matcher.find()) {
			return matcher.group(2);
		}
		return null;
	}

	private static String buildRunScript(String className) {
		// 컴파일 + 실행을 한 번에 수행하는 간단한 셸 스크립트입니다.
		return "#!/bin/sh\n"
				+ "set -e\n"
				+ "javac -encoding UTF-8 " + className + ".java\n"
				+ "java -Dfile.encoding=UTF-8 " + className + " \"$@\"\n";
	}
}
