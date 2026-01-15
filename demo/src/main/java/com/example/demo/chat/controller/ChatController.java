package com.example.demo.chat.controller;

import com.example.demo.chat.dto.ChatMessageDto;
import com.example.demo.chat.dto.ChatRoomDto;
import com.example.demo.chat.service.ChatService;
import com.example.demo.common.ApiResponse;
import com.example.demo.common.CustomException;
import com.example.demo.common.ErrorMessage;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/api/chat/rooms")
    public ApiResponse<ChatRoomDto> createRoom(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new CustomException(ErrorMessage.REQUIRED_FIELD);
        }

        ChatRoomDto room = chatService.createRoom(name);
        return ApiResponse.success("채팅방이 생성되었습니다.", room);
    }

    @GetMapping("/api/chat/rooms")
    public ApiResponse<List<ChatRoomDto>> getRooms() {
        return ApiResponse.success(chatService.getAllRooms());
    }

    @GetMapping("/api/chat/rooms/{roomId}/messages")
    public ApiResponse<List<ChatMessageDto>> getMessages(@PathVariable Long roomId) {
        if (roomId == null || roomId <= 0) {
            throw new CustomException(ErrorMessage.INVALID_INPUT);
        }

        return ApiResponse.success(chatService.getMessages(roomId));
    }

    @GetMapping("/api/chat/rooms/{roomId}/search")
    public ApiResponse<List<ChatMessageDto>> searchMessages(
            @PathVariable Long roomId,
            @RequestParam String keyword) {

        if (roomId == null || roomId <= 0) {
            throw new CustomException(ErrorMessage.INVALID_INPUT);
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CustomException(ErrorMessage.REQUIRED_FIELD);
        }

        return ApiResponse.success(chatService.searchMessages(roomId, keyword));
    }

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            ChatMessageDto message) {

        if (message == null) {
            throw new CustomException(ErrorMessage.INVALID_INPUT);
        }

        if (message.getUserId() == null || message.getContent() == null) {
            throw new CustomException(ErrorMessage.REQUIRED_FIELD);
        }

        chatService.sendMessage(
                roomId,
                message.getUserId(),
                message.getContent()
        );
    }
}