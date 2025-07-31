package com.example.easychat_server.dto;

import lombok.Data;

@Data
public class LoginDto {
    private String easychatId;
    private String password;
}