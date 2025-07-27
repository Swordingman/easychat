package com.example.easychat_server.dto;

import lombok.Data;

@Data
public class LoginDto {
    private String username;
    private String password;
}