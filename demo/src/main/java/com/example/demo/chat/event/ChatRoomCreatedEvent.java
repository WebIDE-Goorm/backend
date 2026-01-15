package com.example.demo.chat.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreatedEvent {
    private Long roomId;
    private String name;
    private LocalDateTime timestamp;
}