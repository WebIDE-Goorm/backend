package com.example.demo.chat.repository;

import com.example.demo.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long roomId);

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :roomId AND m.content LIKE %:keyword%")
    List<ChatMessage> searchMessages(@Param("roomId") Long roomId, @Param("keyword") String keyword);
}