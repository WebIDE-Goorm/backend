package com.example.demo.execution.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 엔드포인트 등록.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
  private final ExecutionWebSocketHandler handler;
  private final ExecutionHandshakeInterceptor interceptor;

  public WebSocketConfig(ExecutionWebSocketHandler handler, ExecutionHandshakeInterceptor interceptor) {
    this.handler = handler;
    this.interceptor = interceptor;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    // runId는 핸드셰이크 인터셉터에서 추출한다.
    registry.addHandler(handler, "/ws/run/{runId}")
            .addInterceptors(interceptor)
            .setAllowedOrigins("*");
  }
}
