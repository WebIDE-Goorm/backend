package com.example.demo.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * TEXT_CHANGE 메시지 - 텍스트 변경 메시지
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TextChangeMessage extends EditorMessage {
    private String content; // 변경된 전체 텍스트 내용
    private Integer version; // 클라이언트가 알고 있는 기존 파일 버전
}
