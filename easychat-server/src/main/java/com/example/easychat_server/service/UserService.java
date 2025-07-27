package com.example.easychat_server.service;

import com.example.easychat_server.model.User;
import com.example.easychat_server.repository.UserRepository;
import com.example.easychat_server.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 注入 Spring Security 提供的密码加密器
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

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
        return jwtUtil.generateToken(user.getUsername(), user.getId());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }
}
