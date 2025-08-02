package com.example.easychat_server.controller;

import com.example.easychat_server.model.Message;
import com.example.easychat_server.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/conversation")
    public ResponseEntity<List<Message>> getConversation(
            @RequestParam Long userId1,
            @RequestParam Long userId2
    ) {
        List<Message> messages = messageService.getConversation(userId1, userId2);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/group_conversation")
    public ResponseEntity<List<Message>> getGroupConversation(@RequestParam Long groupId) {
        // 安全性说明：在真实项目中，这里应该先验证当前登录用户是否是该群的成员
        List<Message> messages = messageService.getGroupConversation(groupId);
        return ResponseEntity.ok(messages);
    }
}
