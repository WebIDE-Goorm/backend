package com.example.demo.chat.publisher;

import com.example.demo.chat.event.ChatMessageEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatEventPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ChannelTopic chatTopic;
    private final ObjectMapper objectMapper;

    public void publishMessage(ChatMessageEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(chatTopic.getTopic(), json);
        } catch (Exception e) {
            throw new RuntimeException("Redis publish 실패", e);
        }
    }
}