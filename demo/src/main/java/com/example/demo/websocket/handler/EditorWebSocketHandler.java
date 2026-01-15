package com.example.demo.websocket.handler;

import com.example.demo.filecontent.entity.FileContent;
import com.example.demo.filecontent.repository.FileContentRepository;
import com.example.demo.websocket.dto.*;
import com.example.demo.websocket.manager.EditorSessionManager;
import com.example.demo.websocket.util.TextDiffUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;

/**
 * 에디터 핸들러
 * 
 * - TEXT_CHANGE 메시지 처리 및 버전 관리
 * - CURSOR_MOVE 메시지 브로드캐스트
 * - TEXT_SYNC 메시지 브로드캐스트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EditorWebSocketHandler extends TextWebSocketHandler {

    private final EditorSessionManager sessionManager;
    private final FileContentRepository fileContentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // fileId와 userId를 추출하여 세션 관리자에 등록 (세션 생성)
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String path = session.getUri().getPath();
        Long fileId = extractFileIdFromPath(path); // fileId 추출
        Long userId = extractUserIdFromSession(session); // userId 추출

        if (fileId == null) {
            log.warn("fileId를 추출할 수 없습니다. path: {}", path);
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        if (userId == null) {
            log.warn("userId를 추출할 수 없습니다. sessionId: {}", session.getId());
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        sessionManager.addSession(fileId, userId, session);
        log.info("WebSocket 연결 성공: fileId={}, userId={}, sessionId={}", fileId, userId, session.getId());
    }

    // 메시지 타입에 따라 분기 처리 [TEXT_CHANGE: handleTextChange(), CURSOR_MOVE: handleCursorMove()]
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            EditorMessage editorMessage = objectMapper.readValue(message.getPayload(), EditorMessage.class);
            
            switch (editorMessage.getType()) {
                case "TEXT_CHANGE":
                    // TEXT_CHANGE --> TextChangeMessage
                    TextChangeMessage textChangeMessage = objectMapper.readValue(
                        message.getPayload(), 
                        TextChangeMessage.class
                    );
                    handleTextChange(session, textChangeMessage);
                    break;
                    
                case "CURSOR_MOVE":
                    // CURSOR_MOVE --> CursorMoveMessage
                    CursorMoveMessage cursorMoveMessage = objectMapper.readValue(
                        message.getPayload(), 
                        CursorMoveMessage.class
                    );
                    handleCursorMove(session, cursorMoveMessage);
                    break;
                    
                default:
                    log.warn("알 수 없는 메시지 타입: {}", editorMessage.getType());
            }
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: sessionId={}, message={}", 
                session.getId(), message.getPayload(), e);
        }
    }

    /**
     * WebSocket 연결이 종료되었을 때 호출
     * - 정리 작업 수행
     * @param session 종료된 WebSocket 세션
     * @param status 종료 상태
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionManager.removeSession(session);
        log.info("WebSocket 연결 종료: sessionId={}, status={}", session.getId(), status);
    }

    /**
     * TEXT_CHANGE 메시지 처리
     * - 변경 사항만 브로드캐스트
     * 
     * 세션의 fileId와 메시지의 fileId 일치 여부 확인
     * DB에서 현재 파일의 최신 버전 조회
     * 새 버전 계산: 기존 버전 + 1 (없으면 1)
     * 새 FileContent 엔티티 생성 및 DB 저장 (전체 텍스트 저장)
     * 이전 텍스트와 새 텍스트를 비교하여 변경 사항 계산
     * 변경 사항만 TEXT_SYNC 메시지로 브로드캐스트 (변경이 없으면 브로드캐스트하지 않음)
     * 
     * @param session WebSocket 세션
     * @param message TEXT_CHANGE 메시지
     */
    protected void handleTextChange(WebSocketSession session, TextChangeMessage message) {
        Long fileId = sessionManager.getFileIdBySession(session);
        
        if (fileId == null || !fileId.equals(message.getFileId())) {
            log.warn("세션의 fileId({})와 메시지의 fileId({})가 일치하지 않습니다.", 
                fileId, message.getFileId());
            return;
        }

        // DB에서 현재 파일의 최신 버전 조회
        FileContent currentContent = fileContentRepository
            .findFirstByFileIdOrderByVersionDesc(fileId)
            .orElse(null);

        // 클라이언트가 가진 버전이 서버의 저장된 버전보다 낮으면 드랍 (버전 충돌 처리)
        if (currentContent != null && message.getVersion() < currentContent.getVersion()) {
            log.info("버전 충돌 발생: fileId={}, 클라이언트 버전={}, 서버 버전={}", 
                fileId, message.getVersion(), currentContent.getVersion());
            return;
        }

        // 새 버전 계산
        // 현재 버전이 있으면 +1, 없으면 1
        Integer nextVersion = currentContent != null ? currentContent.getVersion() + 1 : 1;
        
        // 새 FileContent 엔티티 생성 및 DB 저장 (전체 텍스트 저장)
        FileContent newContent = FileContent.create(fileId, message.getContent(), nextVersion);
        fileContentRepository.save(newContent);
        log.debug("FileContent 저장 완료: fileId={}, version={}", fileId, nextVersion);

        // 이전 텍스트와 비교하여 변경 사항 계산
        String oldText = currentContent != null ? currentContent.getContent() : "";
        String newText = message.getContent();
        
        // Diff 계산
        TextDiffUtil.TextChange change = TextDiffUtil.calculateDiff(oldText, newText);
        
        Long userId = sessionManager.getUserIdBySession(session);
        
        // 변경 사항만 브로드캐스트 (변경이 없으면 브로드캐스트하지 않음)
        if (change != null) {
            broadcastTextSync(fileId, change, nextVersion, userId);
        } else {
            log.debug("변경 사항이 없어 브로드캐스트하지 않음: fileId={}, version={}", fileId, nextVersion);
        }
    }

    /**
     * CURSOR_MOVE 메시지 처리
     * 
     * DB 저장하지 않음
     * 
     * @param session WebSocket 세션
     * @param message CURSOR_MOVE 메시지
     */
    protected void handleCursorMove(WebSocketSession session, CursorMoveMessage message) {
        Long fileId = sessionManager.getFileIdBySession(session);
        
        if (fileId == null || !fileId.equals(message.getFileId())) {
            log.warn("세션의 fileId({})와 메시지의 fileId({})가 일치하지 않습니다.", 
                fileId, message.getFileId());
            return;
        }

        // 같은 fileId의 room에 있는 모든 세션 조회
        Set<WebSocketSession> sessions = sessionManager.getSessionsByFileId(fileId);
        
        // 자신을 제외한 다른 세션들에게만 커서 위치 브로드캐스트
        for (WebSocketSession s : sessions) {
            if (!s.getId().equals(session.getId()) && s.isOpen()) {
                try {
                    String jsonMessage = objectMapper.writeValueAsString(message);
                    s.sendMessage(new TextMessage(jsonMessage));
                } catch (IOException e) {
                    log.error("CURSOR_MOVE 브로드캐스트 실패: sessionId={}, error={}", s.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * TEXT_SYNC 메시지 브로드캐스트 (변경 사항만 전송)
     * 
     * 변경 사항만 브로드캐스트 (다른 위치에서 작업 중인 다른 사용자의 변경사항 건들지 않음)
     * 
     * 모든 세션(자신 포함)에 변경 사항만 전송
     * 
     * @param fileId 파일 ID
     * @param change 변경 사항 (diff 계산 결과)
     * @param version 최신 버전 번호
     * @param userId 변경을 수행한 사용자 ID
     */
    protected void broadcastTextSync(Long fileId, TextDiffUtil.TextChange change, Integer version, Long userId) {
        // TEXT_SYNC 메시지 생성
        TextSyncMessage syncMessage = new TextSyncMessage();
        syncMessage.setType("TEXT_SYNC");
        syncMessage.setFileId(fileId);
        syncMessage.setVersion(version);
        syncMessage.setUserId(userId);
        
        // 변경 사항 정보 설정
        TextSyncMessage.TextChange textChange = new TextSyncMessage.TextChange();
        textChange.setRange(new TextSyncMessage.TextChange.Range(
            change.getRange().getStart(),
            change.getRange().getEnd()
        ));
        textChange.setNewText(change.getNewText());
        syncMessage.setChange(textChange);

        // 같은 file의 room에 있는 모든 세션 조회
        Set<WebSocketSession> sessions = sessionManager.getSessionsByFileId(fileId);
        
        // 모든 세션에 변경 사항 전송 (자신 포함)
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    String jsonMessage = objectMapper.writeValueAsString(syncMessage);
                    session.sendMessage(new TextMessage(jsonMessage));
                } catch (IOException e) {
                    log.error("TEXT_SYNC 브로드캐스트 실패: sessionId={}, fileId={}, version={}, error={}", 
                        session.getId(), fileId, version, e.getMessage());
                }
            }
        }
        
        log.debug("TEXT_SYNC 브로드캐스트 완료: fileId={}, version={}, change={}, sessionCount={}", 
            fileId, version, change, sessions.size());
    }
    
    // 기존 방식 - 전체 텍스트를 브로드캐스트
    // protected void broadcastTextSync(Long fileId, String content, Integer version, Long userId) {
    //     // TEXT_SYNC 메시지 객체 생성
    //     TextSyncMessage syncMessage = new TextSyncMessage();
    //     syncMessage.setType("TEXT_SYNC");
    //     syncMessage.setFileId(fileId);
    //     syncMessage.setContent(content);
    //     syncMessage.setVersion(version);
    //     syncMessage.setUserId(userId);
    //
    //     // 같은 fileId의 room에 있는 모든 세션 조회
    //     Set<WebSocketSession> sessions = sessionManager.getSessionsByFileId(fileId);
    //     
    //     // 모든 세션에 메시지 전송 (자신 포함)
    //     // 모든 클라이언트가 최신 버전을 받아서 동기화해야 함
    //     for (WebSocketSession session : sessions) {
    //         // 연결이 열려있는 세션에만 전송
    //         if (session.isOpen()) {
    //             try {
    //                 // 메시지를 JSON 문자열로 변환하여 전송
    //                 String jsonMessage = objectMapper.writeValueAsString(syncMessage);
    //                 session.sendMessage(new TextMessage(jsonMessage));
    //             } catch (IOException e) {
    //                 // 전송 실패 시 로깅만 하고 계속 진행
    //                 log.error("TEXT_SYNC 브로드캐스트 실패: sessionId={}, fileId={}, version={}", 
    //                     session.getId(), fileId, version, e);
    //             }
    //         }
    //     }
    //     
    //     log.debug("TEXT_SYNC 브로드캐스트 완료: fileId={}, version={}, sessionCount={}", 
    //         fileId, version, sessions.size());
    // }

    /**
     * URL 경로에서 fileId 추출
     * 
     * 
     * @param path WebSocket 연결 URL 경로
     * @return 추출된 fileId, 실패 시 null
     */
    private Long extractFileIdFromPath(String path) {
        try {
            String[] parts = path.split("/");
            
            if (parts.length >= 4 && parts[2].equals("editor")) {
                return Long.parseLong(parts[3]);
            }
        } catch (NumberFormatException e) {
            log.error("fileId 파싱 실패: path={}, error={}", path, e.getMessage());
        }
        return null;
    }

    /**
     * WebSocket 세션에서 userId 추출
     * 
     * @param session WebSocket 세션
     * @return 추출된 userId, 실패 시 null
     */
    private Long extractUserIdFromSession(WebSocketSession session) {
        // 세션 속성에서 userId 조회
        Object userIdObj = session.getAttributes().get("userId");
        
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }

        // String으로 저장된 경우 (혹시 모를 경우 대비)
        if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                log.error("userId 파싱 실패: userId={}, error={}", userIdObj, e.getMessage());
            }
        }

        log.warn("userId를 세션 속성에서 찾을 수 없습니다. sessionId={}, attributes={}", 
            session.getId(), session.getAttributes());
        return null;
    }
}
