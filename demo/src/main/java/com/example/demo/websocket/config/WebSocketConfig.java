package com.example.demo.websocket.config;

import com.example.demo.websocket.handler.EditorWebSocketHandler;
import com.example.demo.websocket.interceptor.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Value;
/**
 * WebSocket 설정 클래스
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final EditorWebSocketHandler editorWebSocketHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Value("${allowed.origins:*}")
    private String[] allowedOrigins;
    /**
     * WebSocket 핸들러 등록
     * 
     * CORS 설정: 현재는 모든 origin 허용 (이후에 특정 도메인으로 제한 예정)
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(editorWebSocketHandler, "/ws/editor/{fileId}")
            .addInterceptors(jwtHandshakeInterceptor) // JWT 토큰 인증 인터셉터 추가
            .setAllowedOrigins(allowedOrigins);
    }
}
