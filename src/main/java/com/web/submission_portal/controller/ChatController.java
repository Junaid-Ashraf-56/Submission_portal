package com.web.submission_portal.controller;

import com.web.submission_portal.entity.ChatMessage;
import com.web.submission_portal.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage handleMessage(
            @DestinationVariable String roomId,
            @Payload ChatMessage message,
            Principal principal) {


        if (principal == null) {
            throw new RuntimeException("WebSocket principal is null — user not authenticated");
        }else{
            System.out.println(principal.getName());
        }
        message.setRoomId(roomId);
        message.setSenderName(principal.getName());
        chatMessageRepository.save(message);

        return message;
    }

    @GetMapping("/chat/{roomId}/history")
    @ResponseBody
    public List<ChatMessage> getHistory(@PathVariable String roomId) {
        return chatMessageRepository.findTop50ByRoomIdOrderBySentAtAsc(roomId);
    }
}