package com.example.demo.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * CURSOR_MOVE 메시지
 * - 실시간으로 다른 사용자의 커서 위치를 화면에 표시
 * 
 * - 서버는 DB에 저장하지 않고, 같은 room의 다른 세션들에게만 브로드캐스트
 * (클라이언트가 커서를 이동할 때마다 서버로 전송)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CursorMoveMessage extends EditorMessage {
    private CursorPosition cursor;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CursorPosition {
        private Integer line;
        private Integer column;
    }
}
