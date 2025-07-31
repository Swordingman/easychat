package com.example.easychat_server.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContactDto {
    // 包含所有 User 的信息
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String easychatId;

    // 额外添加的会话信息
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    // 以后还可以加 unreadCount 等
}
