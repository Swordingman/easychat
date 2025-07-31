package com.example.easychat_server.controller;

import com.example.easychat_server.dto.ContactDto;
import com.example.easychat_server.dto.ContactRequestDto;
import com.example.easychat_server.model.Contact;
import com.example.easychat_server.model.User;
import com.example.easychat_server.service.ContactService;
import com.example.easychat_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    @Autowired
    private ContactService contactService;
    @Autowired
    private UserService userService;

    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@RequestParam Long receiverId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        try {
            return ResponseEntity.ok(contactService.sendRequest(currentUser.getId(), receiverId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getFriendRequests(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<ContactRequestDto> dtos = contactService.getPendingRequestsWithUserInfo(currentUser.getId());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/action")
    public ResponseEntity<?> handleFriendRequest(
            @RequestParam Long requestId,
            @RequestParam String action,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        try {
            Contact result = contactService.handleRequest(requestId, currentUser.getId(), action);
            if (result == null) {
                return ResponseEntity.ok().body("请求已拒绝并删除");
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getContactList(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(contactService.getContacts(currentUser.getId()));
    }

    /**
     * 辅助方法：从认证信息中获取当前用户实体
     */
    private User getCurrentUser(Authentication authentication) {
        String easychatId = authentication.getName();
        return userService.findByEasychatId(easychatId);
    }
}
