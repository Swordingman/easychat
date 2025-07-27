package com.example.easychat_server.config;

import com.example.easychat_server.websocket.AuthHandshakeInterceptor;
import com.example.easychat_server.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket // 开启 WebSocket 支持
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Autowired
    private AuthHandshakeInterceptor authHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat") // 1. 注册处理器到 "/ws/chat" 路径
                .addInterceptors(authHandshakeInterceptor) // 2. 添加我们的握手拦截器
                .setAllowedOrigins("*"); // 3. 允许所有来源的连接（开发时方便，生产环境应配置具体的域名）
    }
}
