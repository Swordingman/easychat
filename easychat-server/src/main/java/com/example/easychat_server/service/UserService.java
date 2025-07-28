package com.example.easychat_server.service;

import com.example.easychat_server.dto.UserUpdateDto;
import com.example.easychat_server.model.User;
import com.example.easychat_server.repository.UserRepository;
import com.example.easychat_server.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Autowired
    private UserRepository userRepository;

    // 注入 Spring Security 提供的密码加密器
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User register(User user) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            // 如果存在，可以抛出一个自定义异常，让 Controller 层捕获
            throw new IllegalArgumentException("用户名 " + user.getUsername() + " 已被注册！");
        }

        // 重要：对密码进行加密存储
        // 永远不要在数据库中存储明文密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置一个默认昵称，如果用户没提供的话
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname("新用户_" + user.getUsername());
        }

        // 设置一个默认头像
        user.setAvatar("https://i.pravatar.cc/150?u=" + user.getUsername());

        return userRepository.save(user);
    }

    public String login(String username, String password) {
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在或密码错误"));

        // 验证密码
        // passwordEncoder.matches 会将传入的明文密码加密后与数据库中的密文密码进行比对
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("用户不存在或密码错误");
        }

        // 登录成功,这里将生成并返回 JWT Token。
        return JwtUtil.generateToken(user.getUsername(), user.getId());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    @Transactional
    public User updateUser(Long userId, UserUpdateDto userUpdateDto) {
        // 1. 查找用户，这步不变
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在，ID: " + userId));

        // 2. 验证并更新昵称
        // 如果 DTO 中有名为 "nickname" 的字段传来...
        if (userUpdateDto.getNickname() != null) {
            // ...并且这个值不是空字符串（去掉前后空格后）
            if (userUpdateDto.getNickname().trim().isEmpty()) {
                // 如果是空字符串，我们可以选择抛出异常，强制前端进行验证
                throw new IllegalArgumentException("昵称不能为空！");
            }
            // 只有在验证通过后，才进行更新
            user.setNickname(userUpdateDto.getNickname().trim());
            log.info("用户 {} 的昵称已更新为: {}", userId, user.getNickname());
        }
        // 如果前端根本没传 nickname 字段，这里就什么也不做，保持旧值。

        // 3. 验证并更新头像
        // 如果 DTO 中有名为 "avatar" 的字段传来...
        if (userUpdateDto.getAvatar() != null) {
            // ...我们甚至可以简单检查一下它是不是一个合法的路径格式
            if (userUpdateDto.getAvatar().startsWith("/uploads/")) {
                user.setAvatar(userUpdateDto.getAvatar());
                log.info("用户 {} 的头像已更新为: {}", userId, user.getAvatar());
            } else if (userUpdateDto.getAvatar().isEmpty()) {
                // 如果传来的是空字符串，可以选择清空头像或保持不变
                // 我们选择保持不变，更加安全
                log.warn("尝试将用户 {} 的头像设置为空字符串，操作被忽略。", userId);
            } else {
                log.warn("为用户 {} 提供了无效的头像路径: {}，操作被忽略。", userId, userUpdateDto.getAvatar());
            }
        }
        // 如果前端根本没传 avatar 字段，这里也什么都不做。

        // 4. 保存更改
        return userRepository.save(user);
    }
}
