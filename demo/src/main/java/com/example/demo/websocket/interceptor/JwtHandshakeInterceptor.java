package com.example.demo.websocket.interceptor;

import com.example.demo.auth.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * JWT 토큰 검증 인터셉터
 * 
 * - WebSocket 연결 전에 JWT 토큰 검증
 * - 토큰에서 username 추출 후 User 조회하여 userId를 세션 속성에 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    
    private static final String TOKEN_PARAM = "token";
    private static final String USER_ID_ATTRIBUTE = "userId";

    /**
     * 핸드셰이크 전 작업 - JWT 토큰 검증
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param wsHandler WebSocket 핸들러
     * @param attributes WebSocket 세션 속성 (여기에 userId 저장)
     * @return true: 검증 성공, false: 검증 실패
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        // 쿼리 파라미터에서 토큰 추출
        URI uri = request.getURI();
        String query = uri.getQuery();
        String token = null; // JWT 토큰
        
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(TOKEN_PARAM)) {
                    try {
                        // URL 디코딩 처리 (특수문자 포함 시)
                        token = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                    } catch (Exception e) {
                        log.warn("URL 디코딩 실패: {}, error={}", keyValue[1], e.getMessage());
                        token = keyValue[1];
                    }
                    break;
                }
            }
        }
        
        // 토큰이 없으면 연결 거부
        if (token == null || token.isEmpty()) {
            log.warn("JWT 토큰을 찾을 수 없습니다. 쿼리 파라미터에 ?token={jwtToken} 형식으로 전달해주세요. uri={}", uri, query);
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false; // 연결 거부
        }
        
        // 토큰 검증
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("유효하지 않은 JWT 토큰입니다. uri={}", uri);
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false; // 연결 거부
        }
        
        try {
            // 토큰에서 username 추출 (JWT의 subject)
            String username = jwtTokenProvider.getClaims(token).getSubject();
            
            if (username == null || username.isEmpty()) {
                log.warn("JWT 토큰에 username이 없습니다. uri={}", uri);
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false; // 연결 거부
            }
            
            // username으로 User 조회
            com.example.demo.auth.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. username=" + username));
            
            // 세션 속성에 userId 저장 (EditorWebSocketHandler에서 사용)
            attributes.put(USER_ID_ATTRIBUTE, user.getId());
            log.info("WebSocket 핸드셰이크 성공: username={}, userId={}, uri={}, query={}", username, user.getId(), uri, query);
            return true; // 검증 성공
        } catch (Exception e) {
            log.error("WebSocket 핸드셰이크 중 오류 발생: uri={}, query={}, error={}", uri, query, e.getMessage());
            response.setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            return false; // 검증 실패
        }
    }

    /**
     * 핸드셰이크 완료 후 호출
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param wsHandler WebSocket 핸들러
     * @param exception 핸드셰이크 중 발생한 예외 (없으면 null)
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 완료 후 추가 작업 없음
        if (exception != null) {
            log.error("WebSocket 핸드셰이크 중 오류 발생: uri={}", request.getURI(), exception);
        } else {
            log.debug("WebSocket 핸드셰이크 완료: uri={}", request.getURI());
        }
    }
}
