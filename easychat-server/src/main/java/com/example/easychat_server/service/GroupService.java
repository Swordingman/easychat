package com.example.easychat_server.service;

import com.example.easychat_server.dto.GroupMemberDto;
import com.example.easychat_server.dto.GroupUpdateDto;
import com.example.easychat_server.model.Group;
import com.example.easychat_server.model.GroupMember;
import com.example.easychat_server.model.User;
import com.example.easychat_server.repository.GroupMemberRepository;
import com.example.easychat_server.repository.GroupRepository;
import com.example.easychat_server.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 创建一个新的群组
     * @param groupName 群名称
     * @param ownerId 群主的ID
     * @param memberIds 初始成员的ID列表 (必须包含群主自己)
     * @return 创建成功后的群组对象
     */
    @Transactional // 这是一个多步操作，必须使用事务
    public Group createGroup(String groupName, Long ownerId, List<Long> memberIds) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("群名称不能为空");
        }
        if (!memberIds.contains(ownerId)) {
            // 为安全起见，确保群主在成员列表中
            memberIds.add(ownerId);
        }

        // 1. 创建并保存 Group 对象
        Group newGroup = new Group();
        newGroup.setGroupName(groupName);
        newGroup.setOwnerId(ownerId);
        // 暂时给一个默认头像
        newGroup.setAvatar("https://i.pravatar.cc/150?u=" + groupName);
        Group savedGroup = groupRepository.save(newGroup);

        // 2. 批量添加群成员
        List<GroupMember> members = new ArrayList<>();
        for (Long memberId : memberIds) {
            GroupMember member = new GroupMember();
            member.setGroupId(savedGroup.getId());
            member.setUserId(memberId);
            // 判断是否为群主
            if (memberId.equals(ownerId)) {
                member.setRole("OWNER");
            } else {
                member.setRole("MEMBER");
            }
            members.add(member);
        }
        groupMemberRepository.saveAll(members);

        return savedGroup;
    }

    /**
     * 根据用户ID，获取他加入的所有群组列表
     * @param userId 用户ID
     * @return 群组列表
     */
    public List<Group> getMyGroups(Long userId) {
        // 1. 先从成员表找到我所在的所有群的ID
        List<Long> myGroupIds = groupMemberRepository.findGroupIdsByUserId(userId);
        if (myGroupIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 2. 根据这些ID，批量查询出所有群的详细信息
        return groupRepository.findAllById(myGroupIds);
    }

    /**
     * 根据群ID，获取群成员列表
     * @param groupId 群ID
     * @return 群成员列表
     */
    public List<GroupMemberDto> getGroupMembers(Long groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        List<Long> userIds = members.stream().map(GroupMember::getUserId).toList();
        List<User> users = userRepository.findAllById(userIds);

        // 将 User 列表转换为 Map，方便快速查找
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));

        return members.stream().map(member -> {
            User user = userMap.get(member.getUserId());
            if (user == null) return null; // 如果用户已注销，则忽略

            GroupMemberDto dto = new GroupMemberDto();
            dto.setMemberId(user.getId());
            dto.setNickname(user.getNickname());
            dto.setAvatar(user.getAvatar());
            dto.setRole(member.getRole());
            return dto;
        }).filter(Objects::nonNull).toList();
    }

    /**
     * 邀请好友加入群聊
     * @param groupId 群ID
     * @param inviterId 邀请者ID (必须是群主或管理员)
     * @param inviteeIds 被邀请的好友ID列表
     */
    @Transactional
    public void inviteMembers(Long groupId, Long inviterId, List<Long> inviteeIds) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("群组不存在"));

        // 1. 安全校验：只有群主能邀请 (未来可以扩展为管理员)
        if (!group.getOwnerId().equals(inviterId)) {
            throw new IllegalStateException("只有群主才能邀请新成员");
        }

        // 2. 找出已经是群成员的用户，避免重复添加
        List<GroupMember> existingMembers = groupMemberRepository.findByGroupId(groupId);
        List<Long> existingMemberIds = existingMembers.stream().map(GroupMember::getUserId).toList();

        // 3. 过滤掉已经是成员的用户，得到真正需要新增的成员ID
        List<Long> newMemberIds = inviteeIds.stream()
                .filter(id -> !existingMemberIds.contains(id))
                .toList();

        if (newMemberIds.isEmpty()) return; // 没有新成员需要添加

        // 4. 批量添加新成员
        List<GroupMember> newMembers = newMemberIds.stream().map(userId -> {
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole("MEMBER");
            return member;
        }).toList();

        groupMemberRepository.saveAll(newMembers);
    }

    /**
     * 将成员移出群聊 (踢人)
     * @param groupId 群ID
     * @param operatorId 操作者ID (必须是群主)
     * @param memberToKickId 被踢的成员ID
     */
    @Transactional
    public void kickMember(Long groupId, Long operatorId, Long memberToKickId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("群组不存在"));

        // 1. 安全校验：只有群主能踢人
        if (!group.getOwnerId().equals(operatorId)) {
            throw new IllegalStateException("只有群主才能移出成员");
        }

        // 2. 不能踢群主自己
        if (operatorId.equals(memberToKickId)) {
            throw new IllegalArgumentException("不能将群主移出群聊");
        }

        // 3. 执行删除
        // (更严谨的做法是先查询是否存在这条记录)
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, memberToKickId);
    }


    /**
     * 更新群信息
     * @param groupId 群ID
     * @param operatorId 操作者ID (必须是群主)
     * @param groupUpdateDto 群信息更新对象
     * @return 更新后的群对象
     */
    @Transactional
    public Group updateGroup(Long groupId, Long operatorId, GroupUpdateDto groupUpdateDto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("群组不存在"));

        // 安全校验：只有群主能修改
        if (!group.getOwnerId().equals(operatorId)) {
            throw new IllegalStateException("只有群主才能修改群信息");
        }

        // 按需更新字段
        if (groupUpdateDto.getGroupName() != null && !groupUpdateDto.getGroupName().trim().isEmpty()) {
            group.setGroupName(groupUpdateDto.getGroupName().trim());
        }
        if (groupUpdateDto.getAvatar() != null) {
            group.setAvatar(groupUpdateDto.getAvatar());
        }
        if (groupUpdateDto.getAnnouncement() != null) {
            group.setAnnouncement(groupUpdateDto.getAnnouncement());
        }

        return groupRepository.save(group);
    }
}
