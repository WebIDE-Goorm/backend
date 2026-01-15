package com.example.demo.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * TEXT_SYNC 메시지
 * 
 * - 서버가 TEXT_CHANGE 처리 후 변경 사항만 브로드캐스트
 * (클라이언트는 받은 변경 사항을 로컬 텍스트에 적용)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TextSyncMessage extends EditorMessage {
    
    private Integer version; // 최신 파일 버전
    private TextChange change; // 변경 사항
    

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextChange {

        private Range range; // 변경 범위
        private String newText; // 새 텍스트
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Range {
            private Integer start; // 변경 시작 위치 (0부터 시작)
            private Integer end; // 변경 끝 위치
        }
    }
}
