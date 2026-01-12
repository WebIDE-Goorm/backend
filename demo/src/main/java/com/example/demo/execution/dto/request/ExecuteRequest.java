package com.example.demo.execution.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 실행 요청 바디.
 */
@Getter
@NoArgsConstructor
public class ExecuteRequest {
  // 실행 언어 (java, js, python)
  private String language;
  // 사용자 소스 코드
  private String code;
  // 표준 입력
  private String input;
}
