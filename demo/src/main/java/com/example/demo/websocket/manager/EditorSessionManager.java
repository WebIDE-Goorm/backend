package com.example.demo.websocket.manager;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 세션 관리자
 * 
 * - 파일 단위로 세션을 그룹화하여 관리
 * - 세션과 fileId, userId의 매핑 관리
 * - 동시성 안전 보장 (ConcurrentHashMap 사용)
 * -- ConcurrentHashMap -> 멀티스레드 환경에서 안전하게 사용할 수 있는 HashMap
 */
@Component
public class EditorSessionManager {


    // 파일별 세션 관리 Map
    private final Map<Long, Set<WebSocketSession>> fileRooms = new ConcurrentHashMap<>();
    // 세션과 fileId 매핑
    private final Map<WebSocketSession, Long> sessionToFileId = new ConcurrentHashMap<>();
    // 세션과 userId 매핑
    private final Map<WebSocketSession, Long> sessionToUserId = new ConcurrentHashMap<>();

    /**
     * 세션을 파일 room에 추가
     * 
     * 세션과 fileId, userId 매핑 저장
     * 
     * @param fileId 파일 ID
     * @param userId 사용자 ID
     * @param session 세션
     */
    public void addSession(Long fileId, Long userId, WebSocketSession session) {
        fileRooms.computeIfAbsent(fileId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionToFileId.put(session, fileId);
        sessionToUserId.put(session, userId);
    }

    /**
     * 세션 제거
     * 
     * @param session 제거할 WebSocket 세션
     */
    public void removeSession(WebSocketSession session) {
        Long fileId = sessionToFileId.remove(session);
        sessionToUserId.remove(session);
        
        if (fileId != null) {
            Set<WebSocketSession> sessions = fileRooms.get(fileId);
            if (sessions != null) {
                // room에서 세션 제거
                sessions.remove(session);
                // room이 비어있으면 Map에서도 제거 = 메모리 절약
                if (sessions.isEmpty()) {
                    fileRooms.remove(fileId);
                }
            }
        }
    }

    // 파일 room의 모든 세션 조회
    public Set<WebSocketSession> getSessionsByFileId(Long fileId) {
        return fileRooms.getOrDefault(fileId, Collections.emptySet());
    }

    // 세션의 fileId 조회
    public Long getFileIdBySession(WebSocketSession session) {
        return sessionToFileId.get(session);
    }

    // 세션의 userId 조회
    public Long getUserIdBySession(WebSocketSession session) {
        return sessionToUserId.get(session);
    }
}
