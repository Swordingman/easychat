package com.example.easychat_server.service;

import com.example.easychat_server.model.Message;
import com.example.easychat_server.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }
    public List<Message> getConversation(Long userId1, Long userId2) {
        return messageRepository.findConversationMessages(userId1, userId2);
    }

    public List<Message> getGroupConversation(Long groupId) {
        return messageRepository.findByReceiverGroupIdOrderByCreateTimeAsc(groupId);
    }
}
