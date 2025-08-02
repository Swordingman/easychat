package com.example.easychat_server.dto;

import lombok.Data;

@Data
public class GroupMemberDto {
    private Long memberId; // 成员的用户ID
    private String nickname; // 成员的用户昵称
    private String avatar; // 成员的用户头像
    private String role; // 成员在群里的角色
    // ... (可以添加更多字段，如群内昵称等)
}
