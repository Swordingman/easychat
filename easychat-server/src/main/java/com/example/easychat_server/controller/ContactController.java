package com.example.easychat_server.controller;

import com.example.easychat_server.dto.ContactDto;
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

    // 将 getContactList 的返回类型改为 ResponseEntity<List<ContactDto>>
    @GetMapping("/list")
    public ResponseEntity<List<ContactDto>> getContactList(@RequestParam Long userId) {
        List<ContactDto> contacts = contactService.getContactsWithLastMessage(userId);
        return ResponseEntity.ok(contacts);
    }
}