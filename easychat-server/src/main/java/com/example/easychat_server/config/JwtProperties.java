package com.example.easychat_server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt") // 绑定配置文件中以 "jwt" 开头的属性
public class JwtProperties {
    private String secret;
}
