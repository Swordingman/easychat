package com.example.easychat_server.websocket;

import com.example.easychat_server.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthHandshakeInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 在握手之前执行，用于验证用户身份
     * @return 如果返回 true，则握手继续；返回 false，则中断连接。
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // 从请求 URI 的查询参数中获取 token
        // 例如 ws://localhost:8080/ws/chat?token=xxx
        String query = request.getURI().getQuery();
        if (query != null && query.startsWith("token=")) {
            String token = query.substring("token=".length());

            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.getClaimsFromToken(token);
                Long userId = claims.get("userId", Long.class);

                // 将 userId 放入 WebSocketSession 的 attributes 中，以便后续处理器使用
                attributes.put("userId", userId);
                log.info("WebSocket 握手成功，用户ID: {}", userId);
                return true;
            }
        }

        log.warn("WebSocket 握手失败，无效的 Token 或缺少 Token。");
        return false; // 验证失败，中断连接
    }

    /**
     * 在握手之后执行
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // do nothing
    }
}
