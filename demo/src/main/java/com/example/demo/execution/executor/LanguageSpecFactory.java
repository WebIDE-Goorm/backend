package com.example.demo.execution.executor;

import org.springframework.stereotype.Component;

/**
 * 언어별 실행 스펙 제공.
 */
@Component
public class LanguageSpecFactory {
  public ExecutionSpec getSpec(String language) {

    switch (language.toLowerCase()) {
      case "java":
        // Java는 컴파일 후 실행
        return new ExecutionSpec("Main.java", "java-runner", "javac Main.java && java Main");

      case "js":
      case "javascript":
        // JavaScript는 즉시 실행
        return new ExecutionSpec("main.js", "node-runner", "node main.js");

      case "python":
        // Python은 -u로 버퍼링 방지
        return new ExecutionSpec("main.py", "python-runner", "python -u main.py");

      default:
        throw new IllegalArgumentException("지원하지 않는 언어");
    }
  }
}
