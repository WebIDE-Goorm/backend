package com.example.demo.chat.subscriber;

import com.example.demo.auth.User;
import com.example.demo.auth.UserRepository;
import com.example.demo.chat.entity.ChatMessage;
import com.example.demo.chat.entity.ChatRoom;
import com.example.demo.chat.event.ChatMessageEvent;
import com.example.demo.chat.repository.ChatMessageRepository;
import com.example.demo.chat.repository.ChatRoomRepository;
import com.example.demo.common.CustomException;
import com.example.demo.common.ErrorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventSubscriber {

    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository roomRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void onMessage(String message) {
        try {
            ChatMessageEvent event =
                    objectMapper.readValue(message, ChatMessageEvent.class);

            ChatMessage savedMessage = saveMessage(event);

            messagingTemplate.convertAndSend(
                    "/topic/chat/" + event.getRoomId(),
                    event
            );

        } catch (Exception e) {
            throw new CustomException(ErrorMessage.CHAT_SEND_ERROR);
        }
    }


    private ChatMessage saveMessage(ChatMessageEvent event) {
        ChatRoom room = roomRepository.findById(event.getRoomId())
                .orElseThrow(() -> new CustomException(ErrorMessage.CHAT_LOAD_ERROR));

        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        ChatMessage message = new ChatMessage();
        message.setChatRoom(room);
        message.setUser(user);
        message.setContent(event.getContent());

        return messageRepository.save(message);
    }
}