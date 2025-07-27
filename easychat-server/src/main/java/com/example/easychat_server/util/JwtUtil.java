package com.example.easychat_server.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // 1. 设置密钥 (Secret Key) - 必须足够复杂和长，并且保密！
    // Keys.secretKeyFor() 会生成一个安全的密钥。在生产环境中，这个密钥应该从配置文件中读取。
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 2. 设置 Token 的过期时间 (例如：24 小时)
    private static final long EXPIRATION_TIME = 86400_000; // 24 * 60 * 60 * 1000 毫秒

    /**
     * 生成 JWT Token
     * @param username 用户名
     * @param userId 用户ID
     * @return 生成的 Token 字符串
     */
    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username) // 主题，通常是用户名
                .claim("userId", userId) // 添加自定义声明 (payload)，例如用户ID
                .setIssuedAt(now) // 签发时间
                .setExpiration(expiryDate) // 过期时间
                .signWith(SECRET_KEY) // 使用我们的密钥进行签名
                .compact();
    }

    /**
     * 从 Token 中解析出 Claims (包含所有声明)
     * @param token JWT Token
     * @return Claims 对象
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 验证 Token 是否有效且未过期
     * @param token JWT Token
     * @return 如果有效则返回 true
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 如果解析失败（如过期、签名不匹配等），则会抛出异常
            return false;
        }
    }
}
