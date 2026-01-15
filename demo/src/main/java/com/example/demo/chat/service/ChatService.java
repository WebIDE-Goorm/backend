package com.example.demo.chat.service;

import com.example.demo.auth.User;
import com.example.demo.auth.UserRepository;
import com.example.demo.chat.dto.ChatMessageDto;
import com.example.demo.chat.dto.ChatRoomDto;
import com.example.demo.chat.entity.ChatRoom;
import com.example.demo.chat.event.ChatMessageEvent;
import com.example.demo.chat.publisher.ChatEventPublisher;
import com.example.demo.chat.repository.ChatMessageRepository;
import com.example.demo.chat.repository.ChatRoomRepository;
import com.example.demo.common.CustomException;
import com.example.demo.common.ErrorMessage;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatEventPublisher eventPublisher;

    @Transactional
    public ChatRoomDto createRoom(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new CustomException(ErrorMessage.REQUIRED_FIELD);
        }

        ChatRoom room = new ChatRoom();
        room.setName(name);
        ChatRoom saved = chatRoomRepository.save(room);
        return new ChatRoomDto(saved.getId(), saved.getName(), saved.getCreatedAt());
    }

    public void sendMessage(Long roomId, Long userId, String content) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorMessage.CHAT_LOAD_ERROR));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        if (content == null || content.trim().isEmpty()) {
            throw new CustomException(ErrorMessage.REQUIRED_FIELD);
        }

        ChatMessageEvent event = new ChatMessageEvent(
                roomId,
                userId,
                user.getUsername(),
                content,
                LocalDateTime.now()
        );

        try {
            eventPublisher.publishMessage(event);
        } catch (Exception e) {
            throw new CustomException(ErrorMessage.CHAT_SEND_ERROR);
        }
    }

    public List<ChatRoomDto> getAllRooms() {
        return chatRoomRepository.findAll().stream()
                .map(room -> new ChatRoomDto(
                        room.getId(),
                        room.getName(),
                        room.getCreatedAt()))
                .toList();
    }

    public List<ChatMessageDto> getMessages(Long roomId) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorMessage.CHAT_LOAD_ERROR));

        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(msg -> new ChatMessageDto(
                        msg.getId(),
                        msg.getChatRoom().getId(),
                        msg.getUser().getId(),
                        msg.getUser().getUsername(),
                        msg.getContent(),
                        msg.getCreatedAt()))
                .toList();
    }

    public List<ChatMessageDto> searchMessages(Long roomId, String keyword) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorMessage.CHAT_LOAD_ERROR));

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CustomException(ErrorMessage.INVALID_INPUT);
        }

        return chatMessageRepository.searchMessages(roomId, keyword).stream()
                .map(msg -> new ChatMessageDto(
                        msg.getId(),
                        msg.getChatRoom().getId(),
                        msg.getUser().getId(),
                        msg.getUser().getUsername(),
                        msg.getContent(),
                        msg.getCreatedAt()))
                .toList();
    }
}