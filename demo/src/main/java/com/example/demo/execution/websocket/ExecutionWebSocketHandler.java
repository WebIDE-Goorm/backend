package com.example.demo.execution.websocket;

import com.example.demo.execution.model.ExecutionSession;
import com.example.demo.execution.service.ExecutionService;
import com.example.demo.execution.service.ExecutionSessionRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * stdout/stderr 스트리밍과 입력 전달을 담당한다.
 */
@Component
public class ExecutionWebSocketHandler extends TextWebSocketHandler {
  private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
  private final ExecutionService executionService;
  private final ExecutionSessionRegistry registry;
  private final ObjectMapper objectMapper;

  public ExecutionWebSocketHandler(
          ExecutionService executionService,
          ExecutionSessionRegistry registry,
          ObjectMapper objectMapper
  ) {
    this.executionService = executionService;
    this.registry = registry;
    this.objectMapper = objectMapper;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    Object runId = session.getAttributes().get("runId");
    if (runId != null) {
      // runId에 해당하는 세션을 등록
      sessions.put(runId.toString(), session);
      ExecutionSession executionSession = registry.get(runId.toString());
      if (executionSession != null) {
        try {
          String payload = objectMapper.writeValueAsString(
                  java.util.Map.of("type", "status", "data", executionSession.getStatus().name())
          );
          send(runId.toString(), payload);
        } catch (Exception ignored) {
        }
      }
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    Object runId = session.getAttributes().get("runId");
    if (runId != null) {
      sessions.remove(runId.toString());
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    Object runId = session.getAttributes().get("runId");
    if (runId == null) {
      return;
    }
    try {
      // 클라이언트가 보낸 입력/중단 메시지 처리
      JsonNode root = objectMapper.readTree(message.getPayload());
      String type = root.path("type").asText();
      if ("input".equals(type)) {
        String data = root.path("data").asText("");
        executionService.sendInput(runId.toString(), data);
      } else if ("stop".equals(type)) {
        executionService.stop(runId.toString());
      }
    } catch (Exception ignored) {
    }
  }

  // 서버 -> 클라이언트 메시지 전송
  public void send(String runId, String message) {
    WebSocketSession session = sessions.get(runId);
    if (session == null || !session.isOpen()) {
      return;
    }
    try {
      session.sendMessage(new TextMessage(message));
    } catch (IOException ignored) {
    }
  }

  // 세션 종료
  public void close(String runId) {
    WebSocketSession session = sessions.remove(runId);
    if (session == null || !session.isOpen()) {
      return;
    }
    try {
      session.close();
    } catch (IOException ignored) {
    }
  }
}
