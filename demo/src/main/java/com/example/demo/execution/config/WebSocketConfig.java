package com.example.demo.execution.config;

import com.example.demo.execution.websocket.RealtimeCompileHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	private final RealtimeCompileHandler realtimeCompileHandler;
	
	public WebSocketConfig(RealtimeCompileHandler realtimeCompileHandler) {
		this.realtimeCompileHandler = realtimeCompileHandler;
	}
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(realtimeCompileHandler, "/ws/compile")
			.setAllowedOrigins("*");
	}
}
