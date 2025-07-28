package com.example.easychat_server.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    // 只包含允许客户端修改的字段
    private String nickname;
    private String avatar;
}