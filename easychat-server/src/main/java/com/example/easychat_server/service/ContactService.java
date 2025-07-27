package com.example.easychat_server.service;

import com.example.easychat_server.model.Contact;
import com.example.easychat_server.model.User;
import com.example.easychat_server.repository.ContactRepository;
import com.example.easychat_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private UserRepository userRepository;

    public List<User> getContacts(Long currentUserId) {
        List<Contact> relations = contactRepository.findByUserIdAOrUserIdB(currentUserId, currentUserId);
        // 从关系列表中提取出所有好友的ID
        List<Long> friendIds = relations.stream()
                .map(contact -> contact.getUserIdA().equals(currentUserId) ? contact.getUserIdB() : contact.getUserIdA())
                .collect(Collectors.toList());

        if (friendIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 根据好友ID列表批量查询好友的用户信息
        return userRepository.findAllById(friendIds);
    }
}