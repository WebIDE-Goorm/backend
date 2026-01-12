package com.example.demo.execution.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 실행 시작 응답.
 */
@Getter
@AllArgsConstructor
public class ExecuteStartResponse {
  // WebSocket 연결에 사용할 실행 ID
  private String runId;
}
