package com.example.easychat_server.config;

import com.example.easychat_server.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User; // 使用 Spring Security 的 User
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 继承 OncePerRequestFilter 保证每次请求只执行一次

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 从请求头中获取 Authorization 字段
        final String authHeader = request.getHeader("Authorization");

        // 2. 如果请求头为空，或不以 "Bearer " 开头，则直接放行，让后续的过滤器处理
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 提取 JWT Token (去掉 "Bearer " 前缀)
        final String jwt = authHeader.substring(7);

        // 4. 验证 Token
        if (JwtUtil.validateToken(jwt)) {
            // 如果 Token 有效，且当前安全上下文中没有认证信息
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = JwtUtil.getClaimsFromToken(jwt);
                String username = claims.getSubject();

                // 这里我们为了通过 Spring Security 的流程，创建了一个 UserDetails 对象
                // 我们只关心用户名，密码为空，权限列表为空
                UserDetails userDetails = new User(username, "", Collections.emptyList());

                // 创建一个认证通过的 Authentication Token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 将这个 Authentication Token 设置到安全上下文中
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 5. 无论 Token 验证成功与否，都继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}
