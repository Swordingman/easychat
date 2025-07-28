package com.example.easychat_server.controller;

import com.example.easychat_server.dto.LoginDto;
import com.example.easychat_server.dto.UserUpdateDto;
import com.example.easychat_server.model.User;
import com.example.easychat_server.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@RestController // 声明这是一个 RESTful风格的 Controller，所有方法默认返回 JSON
@RequestMapping("/api/user") // 所有在这个类里的请求路径都以 /api/user 开头
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // POST /api/user/register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            // 注册成功后，不应该返回密码等敏感信息
            registeredUser.setPassword(null);
            return ResponseEntity.ok(registeredUser);
        } catch (IllegalArgumentException e) {
            // 如果 service 层抛出了异常（比如用户名已存在），则返回一个错误响应
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST /api/user/login
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto) {
        try {
            String token = userService.login(loginDto.getUsername(), loginDto.getPassword());

            User user = userService.findByUsername(loginDto.getUsername());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            responseData.put("userInfo", userInfo);

            return ResponseEntity.ok(responseData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage()); // 401 Unauthorized
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto userUpdateDto) {
        log.info("===> 请求已进入 updateUser(Long id, ...) 方法,收到的id是: {}", id);
        try {
            User updatedUser = userService.updateUser(id, userUpdateDto);
            updatedUser.setPassword(null); // 注册成功后，不应该返回密码等敏感信息
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
