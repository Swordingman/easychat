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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthHandshakeInterceptor.class);

    /**
     * 在握手之前执行，用于验证用户身份
     * @return 如果返回 true，则握手继续；返回 false，则中断连接。
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        String query = request.getURI().getQuery();

        // 防止 null pointer
        if (query == null || query.isEmpty()) {
            log.warn("WebSocket 握手失败：没有 query 参数");
            return false;
        }

        Map<String, String> queryParams = Arrays.stream(query.split("&"))
                .map(s -> s.split("=", 2))
                .filter(pair -> pair.length == 2)
                .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));

        String token = queryParams.get("token");

        try {
            if (token != null && JwtUtil.validateToken(token)) {
                Claims claims = JwtUtil.getClaimsFromToken(token);
                Long userId = claims.get("userId", Long.class);
                attributes.put("userId", userId);
                log.info("WebSocket 握手成功，用户 ID：{}", userId);
                return true;
            }
        } catch (Exception e) {
            log.error("WebSocket 握手失败，无效或过期的 Token：{}", e.getMessage());
        }

        log.info("尝试握手: URI={}, headers={}", request.getURI(), request.getHeaders());
        log.warn("WebSocket 握手失败，无效的 Token 或缺少 Token。");
        return false;
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
