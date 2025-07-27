package com.example.easychat_server.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.slf4j.Logger;   // 引入 Logger
import org.slf4j.LoggerFactory; // 引入 LoggerFactory

public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // 关键：所有核心字段都改为 static final
    private static final SecretKey SECRET_KEY;
    private static final long EXPIRATION_TIME = 86400_000; // 24 hours

    // 使用静态初始化块，确保在类加载时只执行一次
    static {
        // 注意：这里我们硬编码了密钥。这是一个临时的、为了100%解决问题的做法。
        // 理论上，可以将 JwtProperties 注入到另一个Bean，然后通过静态方法设置这个值，但会增加复杂性。
        // 为了排除所有不确定性，我们先用最原始的方式。
        String secretString = "ThisIsMySuperSecretKeyForEasyChatApplicationWhichIsVeryLongAndSecure12345";
        SECRET_KEY = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
        log.info("JwtUtil a été initialisé avec une clé secrète statique.");
    }

    // 所有方法都改为 static
    public static String generateToken(String username, Long userId) {
        log.info("【生成Token】使用的 SecretKey Hash: {}", SECRET_KEY.hashCode());
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean validateToken(String token) {
        log.info("【验证Token】使用的 SecretKey Hash: {}", SECRET_KEY.hashCode());
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("【JwtUtil】Token 验证失败，异常类型: {}, 异常信息: {}", e.getClass().getName(), e.getMessage());
            return false;
        }
    }
}
