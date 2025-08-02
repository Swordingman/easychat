package com.example.easychat_server.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateGroupDto {
    private String groupName;
    private List<Long> memberIds; // 直接定义为 List<Long>
}