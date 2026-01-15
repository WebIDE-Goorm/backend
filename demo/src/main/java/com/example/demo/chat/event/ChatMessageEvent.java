package com.example.demo.chat.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEvent {
    private Long roomId;
    private Long userId;
    private String username;
    private String content;
    private LocalDateTime timestamp;
}
