package com.example.demo.execution.service;

import com.example.demo.execution.model.ExecutionSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 실행 세션 저장소.
 */
@Component
public class ExecutionSessionRegistry {
  private final Map<String, ExecutionSession> sessions = new ConcurrentHashMap<>();

  // 세션 등록
  public void put(ExecutionSession session) {
    sessions.put(session.getRunId(), session);
  }

  // 세션 조회
  public ExecutionSession get(String runId) {
    return sessions.get(runId);
  }

  // 세션 제거
  public ExecutionSession remove(String runId) {
    return sessions.remove(runId);
  }
}
