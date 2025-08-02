package com.example.easychat_server.controller;

import com.example.easychat_server.dto.CreateGroupDto;
import com.example.easychat_server.dto.GroupMemberDto;
import com.example.easychat_server.dto.GroupUpdateDto;
import com.example.easychat_server.model.Group;
import com.example.easychat_server.model.GroupMember;
import com.example.easychat_server.model.User;
import com.example.easychat_server.repository.GroupMemberRepository;
import com.example.easychat_server.repository.GroupRepository;
import com.example.easychat_server.service.GroupService;
import com.example.easychat_server.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group")
public class GroupController {
    Logger log = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService; // 需要它来获取当前用户

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;


    /**
     * 创建群组
     * 前端需要传递一个 JSON Body，包含 groupName 和 memberIds
     */
    @PostMapping("/create")
    public ResponseEntity<?> createGroup(
            // 2. 将方法参数类型改为 @RequestBody CreateGroupDto
            @RequestBody CreateGroupDto createGroupDto,
            Authentication authentication
    ) {
        try {
            User currentUser = getCurrentUser(authentication);

            // 3. 直接从 DTO 中获取强类型的数据，不再需要强制转换！
            Group newGroup = groupService.createGroup(
                    createGroupDto.getGroupName(),
                    currentUser.getId(),
                    createGroupDto.getMemberIds()
            );

            return ResponseEntity.ok(newGroup);
        } catch (Exception e) {
            // 打印出详细的错误日志，方便调试
            log.error("创建群组失败", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取我加入的群组列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<Group>> getMyGroups(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(groupService.getMyGroups(currentUser.getId()));
    }

    private User getCurrentUser(Authentication authentication) {
        String easychatId = authentication.getName();
        return userService.findByEasychatId(easychatId);
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberDto>> getGroupMembers(@PathVariable Long groupId) {
        // 安全性：同样需要验证当前用户是否是该群成员
        return ResponseEntity.ok(groupService.getGroupMembers(groupId));
    }

    @PostMapping("/{groupId}/invite")
    public ResponseEntity<?> inviteMembers(
            @PathVariable Long groupId,
            @RequestBody List<Long> memberIds,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        try {
            groupService.inviteMembers(groupId, currentUser.getId(), memberIds);
            return ResponseEntity.ok().body("邀请成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/kick")
    public ResponseEntity<?> kickMember(
            @PathVariable Long groupId,
            @RequestParam Long memberId,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        try {
            groupService.kickMember(groupId, currentUser.getId(), memberId);
            return ResponseEntity.ok().body("成员已被踢出群组");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(
            @PathVariable Long groupId,
            @RequestBody GroupUpdateDto groupUpdateDto,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        try {
            Group updatedGroup = groupService.updateGroup(groupId, currentUser.getId(), groupUpdateDto);
            return ResponseEntity.ok(updatedGroup);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
