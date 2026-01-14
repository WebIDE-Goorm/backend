package com.example.demo.execution.execute;

import com.example.demo.execution.model.LanguageType;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 요청된 언어에 맞는 실행기로 코드를 전달합니다.
 */
@Component
public class ScriptExecutor {
	private final Map<LanguageType, LanguageExecutor> executors = new EnumMap<>(LanguageType.class);
	public ScriptExecutor(List<LanguageExecutor> executors) {
		if(executors != null) {
			for(LanguageExecutor executor : executors) {
				this.executors.put(executor.getLanguage(), executor);
			}
		}
	}

	public ScriptExecutionResult execute(LanguageType language, String code, Object[] params, InputStream stdin,
			OutputStream stdout, OutputStream stderr, Consumer<Process> processConsumer) {
		LanguageType resolved = (language != null) ? language : LanguageType.JAVA;
		if(code == null || code.trim().isEmpty()) {
			return new ScriptExecutionResult(false, -1, "", "", "code is required");
		}
		LanguageExecutor executor = executors.get(resolved);
		if(executor == null) {
			return new ScriptExecutionResult(false, -1, "", "", "executor not available for " + resolved.name().toLowerCase());
		}
		String[] args = ExecutionArgumentUtil.toStringArgs(params);
		return executor.execute(code, args, stdin, stdout, stderr, processConsumer);
	}
}
