package com.example.demo.websocket.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 메시지의 기본 클래스
 * 
 * - 모든 에디터 메시지의 공통 필드 정의
 * - Jackson의 다형성 지원을 통해 메시지 타입에 따라 적절한 하위 클래스로 역직렬화
 * 
 * - JSON의 "type" 필드를 기반으로 적절한 하위 클래스로 변환
 * - {"type": "TEXT_CHANGE", ...} → TextChangeMessage로 변환
 * - {"type": "TEXT_SYNC", ...} → TextSyncMessage로 변환
 * - {"type": "CURSOR_MOVE", ...} → CursorMoveMessage로 변환
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextChangeMessage.class, name = "TEXT_CHANGE"), // 텍스트 변경 메시지
    @JsonSubTypes.Type(value = TextSyncMessage.class, name = "TEXT_SYNC"), // 텍스트 동기화 메시지
    @JsonSubTypes.Type(value = CursorMoveMessage.class, name = "CURSOR_MOVE"), // 커서 이동 메시지
})
public abstract class EditorMessage {
    private String type;
    
    private Long fileId; // 편집 중인 파일
    private Long userId; // 메시지를 보낸 사용자
}
