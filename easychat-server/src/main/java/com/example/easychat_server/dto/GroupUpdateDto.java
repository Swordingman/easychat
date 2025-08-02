package com.example.easychat_server.dto;

import lombok.Data;

@Data
public class GroupUpdateDto {
    private String groupName;
    private String avatar;
    private String announcement;
}
