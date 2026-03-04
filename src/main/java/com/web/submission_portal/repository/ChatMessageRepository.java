package com.web.submission_portal.repository;

import com.web.submission_portal.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop50ByRoomIdOrderBySentAtAsc(String roomId);
}