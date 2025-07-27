package com.example.easychat_server.service;

import com.example.easychat_server.dto.ContactDto;
import com.example.easychat_server.model.Contact;
import com.example.easychat_server.model.Message;
import com.example.easychat_server.model.User;
import com.example.easychat_server.repository.ContactRepository;
import com.example.easychat_server.repository.MessageRepository;
import com.example.easychat_server.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository; // 注入 MessageRepository

    // 将 getContacts 方法的返回类型改为 List<ContactDto>
    public List<ContactDto> getContactsWithLastMessage(Long currentUserId) {
        // 1. 获取所有好友的 User 对象
        List<Contact> relations = contactRepository.findByUserIdAOrUserIdB(currentUserId, currentUserId);
        List<Long> friendIds = relations.stream()
                .map(contact -> contact.getUserIdA().equals(currentUserId) ? contact.getUserIdB() : contact.getUserIdA())
                .collect(Collectors.toList());

        if (friendIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> friends = userRepository.findAllById(friendIds);

        // 2. 将 User 列表转换为 ContactDto 列表，并填充最后一条消息
        return friends.stream().map(friend -> {
            // 创建 DTO 并从 User 复制属性
            ContactDto dto = new ContactDto();
            BeanUtils.copyProperties(friend, dto);

            // 3. 为每个好友查询最后一条消息
            // 注意：这里N+1查询问题，后续可以优化
            Optional<Message> lastMessageOpt = messageRepository.findTopBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreateTimeDesc(
                    currentUserId, friend.getId(), friend.getId(), currentUserId
            );

            // 4. 如果存在最后一条消息，则填充到 DTO 中
            if (lastMessageOpt.isPresent()) {
                Message lastMessage = lastMessageOpt.get();
                dto.setLastMessage(lastMessage.getContent());
                dto.setLastMessageTime(lastMessage.getCreateTime());
            } else {
                dto.setLastMessage("开始聊天吧！"); // 如果没有聊天记录，给个默认值
            }
            return dto;
        }).collect(Collectors.toList());
    }
}