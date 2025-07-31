package com.example.easychat_server.service;

import com.example.easychat_server.dto.ContactDto;
import com.example.easychat_server.dto.ContactRequestDto;
import com.example.easychat_server.model.Contact;
import com.example.easychat_server.model.Message;
import com.example.easychat_server.model.User;
import com.example.easychat_server.repository.ContactRepository;
import com.example.easychat_server.repository.MessageRepository;
import com.example.easychat_server.repository.UserRepository;
import jakarta.transaction.Transactional;
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
        // 1. 【核心改造】使用我们新的 findAllFriends 方法，只获取已接受的好友
        List<Contact> relations = contactRepository.findAllFriends(currentUserId, "ACCEPTED");

        // 2. 提取所有好友的 ID
        List<Long> friendIds = relations.stream()
                .map(contact -> contact.getUserIdA().equals(currentUserId) ? contact.getUserIdB() : contact.getUserIdA())
                .collect(Collectors.toList());

        if (friendIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 3. 批量查询好友的用户信息
        List<User> friends = userRepository.findAllById(friendIds);

        // 4. 将 User 列表转换为 ContactDto 列表，并填充最后一条消息 (这部分逻辑完全不变)
        return friends.stream().map(friend -> {
            ContactDto dto = new ContactDto();
            BeanUtils.copyProperties(friend, dto);

            // 查询最后一条消息
            Optional<Message> lastMessageOpt = messageRepository.findTopBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreateTimeDesc(
                    currentUserId, friend.getId(), friend.getId(), currentUserId
            );

            if (lastMessageOpt.isPresent()) {
                Message lastMessage = lastMessageOpt.get();
                dto.setLastMessage(lastMessage.getContent());
                // 可以在这里根据消息类型简化消息内容，比如 "[图片]"
                if ("IMAGE".equals(lastMessage.getMessageType())) {
                    dto.setLastMessage("[图片]");
                } else if ("FILE".equals(lastMessage.getMessageType())) {
                    dto.setLastMessage("[文件]");
                } else if ("VIDEO".equals(lastMessage.getMessageType())) {
                    dto.setLastMessage("[视频]");
                }
                dto.setLastMessageTime(lastMessage.getCreateTime());
            } else {
                dto.setLastMessage("开始聊天吧！");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Contact sendRequest(Long requesterId, Long receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new IllegalArgumentException("不能添加自己为好友");
        }

        // 使用新的查询方法检查关系
        Optional<Contact> existingRelation = contactRepository.findExistingRelation(requesterId, receiverId);
        if (existingRelation.isPresent()) {
            Contact contact = existingRelation.get();
            if ("ACCEPTED".equals(contact.getStatus())) {
                throw new IllegalArgumentException("你们已经是好友了");
            } else if ("PENDING".equals(contact.getStatus())) {
                throw new IllegalArgumentException("已发送过好友请求，请耐心等待对方同意");
            } else { // REJECTED, etc.
                // 允许重新发送请求，可以更新旧记录或创建新记录，这里我们选择更新
                contact.setUserIdA(requesterId); // 确保方向正确
                contact.setUserIdB(receiverId);
                contact.setStatus("PENDING");
                return contactRepository.save(contact);
            }
        }

        Contact request = new Contact();
        request.setUserIdA(requesterId); // 请求发起方
        request.setUserIdB(receiverId); // 请求接收方
        request.setStatus("PENDING");
        return contactRepository.save(request);
    }

    public List<ContactRequestDto> getPendingRequestsWithUserInfo(Long userId) {
        List<Contact> requests = contactRepository.findByUserIdBAndStatus(userId, "PENDING");

        return requests.stream().map(request -> {
            ContactRequestDto dto = new ContactRequestDto();
            dto.setRequestId(request.getId());

            // 根据请求者ID(userIdA)查询用户信息
            User requester = userRepository.findById(request.getUserIdA())
                    .orElse(null); // 如果用户不存在，则为null
            if (requester != null) {
                requester.setPassword(null); // 过滤敏感信息
            }
            dto.setRequesterInfo(requester);

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Contact handleRequest(Long requestId, Long currentUserId, String action) {
        Contact request = contactRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("请求不存在"));

        // 安全校验：确保是接收者本人在操作这个请求
        if (!request.getUserIdB().equals(currentUserId)) {
            throw new IllegalStateException("无权操作此好友请求");
        }

        if ("accept".equalsIgnoreCase(action)) {
            request.setStatus("ACCEPTED");
        } else if ("reject".equalsIgnoreCase(action)) {
            // 简单地删除被拒绝的请求，让界面更干净，也允许对方重新申请
            contactRepository.delete(request);
            return null; // 返回 null 表示已删除
        } else {
            throw new IllegalArgumentException("无效的操作");
        }
        return contactRepository.save(request);
    }

    public List<User> getContacts(Long currentUserId) {
        List<Contact> relations = contactRepository.findAllFriends(currentUserId, "ACCEPTED");

        List<Long> friendIds = relations.stream()
                .map(contact -> contact.getUserIdA().equals(currentUserId) ? contact.getUserIdB() : contact.getUserIdA())
                .collect(Collectors.toList());

        if (friendIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> friends = userRepository.findAllById(friendIds);
        friends.forEach(f -> f.setPassword(null));
        return friends;
    }
}