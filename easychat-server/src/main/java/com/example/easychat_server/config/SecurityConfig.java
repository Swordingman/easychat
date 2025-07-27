package com.example.easychat_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 暂时禁用 Spring Security 的默认登录页和安全策略，以便我们的API可以被访问
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 禁用 CSRF，因为我们是API服务，不使用 cookie session
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // 允许所有 /api/** 的请求
                        .requestMatchers("/ws/**").permitAll()
                        .anyRequest().authenticated() // 其他请求需要认证 (目前没有其他请求)
                );
        return http.build();
    }
}
