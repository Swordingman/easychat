package com.example.easychat_server.controller;

import com.example.easychat_server.model.User;
import com.example.easychat_server.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    @Autowired
    private ContactService contactService;

    // 我们需要一种方式来获取当前登录用户的ID，最标准的方式是从JWT Filter设置的上下文中获取
    // 为了简化，我们暂时先不写 JWT Filter，而是在开发阶段通过参数传递
    // 警告：这只是临时做法！生产环境不安全！
    @GetMapping("/list")
    public ResponseEntity<?> getContactList(@RequestParam Long userId) {
        List<User> contacts = contactService.getContacts(userId);
        // 过滤掉密码等敏感信息
        contacts.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(contacts);
    }
}