package com.example.demo.execution.execute;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import com.example.demo.execution.model.LanguageType;

/**
 * 언어별 실행기를 위한 공통 인터페이스.
 */
public interface LanguageExecutor {
	LanguageType getLanguage();

	ScriptExecutionResult execute(String code, String[] args, InputStream stdin, OutputStream stdout, OutputStream stderr, Consumer<Process> processConsumer);
}
