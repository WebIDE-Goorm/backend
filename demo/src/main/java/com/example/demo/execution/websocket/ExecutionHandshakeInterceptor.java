package com.example.demo.execution.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 경로에서 runId를 추출한다.
 */
@Component
public class ExecutionHandshakeInterceptor implements HandshakeInterceptor {

  /*
  1️⃣ TCP 3-way handshake
  2️⃣ HTTP GET (Upgrade: websocket)
  3️⃣ beforeHandshake  ← 여기!!
  4️⃣ HTTP 101 Switching Protocols
  5️⃣ WebSocket 연결 성립
  6️⃣ afterHandshake   ← 여기!!
  7️⃣ 실제 메시지 통신 시작
   */

  @Override
  public boolean beforeHandshake( // 클라이언트가 WebSocket 업그레이드 요청 시.
          ServerHttpRequest request,
          ServerHttpResponse response,
          WebSocketHandler wsHandler,
          Map<String, Object> attributes
  ) {
    // 경로 형식: /ws/run/{runId}
    String path = request.getURI().getPath();
    String[] parts = path.split("/");
    if (parts.length == 0) {
      return false;
    }

    String runId = parts[parts.length - 1];
    if (runId == null || runId.isBlank()) {
      return false;
    }

    attributes.put("runId", runId);
    return true;
  }

  @Override
  public void afterHandshake(
          ServerHttpRequest request,
          ServerHttpResponse response,
          WebSocketHandler wsHandler,
          Exception exception
  ) {
  }
}
