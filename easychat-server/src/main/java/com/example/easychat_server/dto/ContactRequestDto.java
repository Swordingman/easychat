package com.example.easychat_server.dto;

import com.example.easychat_server.model.User;
import lombok.Data;

@Data
public class ContactRequestDto {
    private Long requestId; // 好友请求记录本身的ID
    private User requesterInfo; // 请求者的完整用户信息
}