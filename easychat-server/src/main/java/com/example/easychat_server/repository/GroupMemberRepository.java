package com.example.easychat_server.repository;

import com.example.easychat_server.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    /**
     * 根据用户ID，查找他所在的所有群组的ID
     * @param userId 用户ID
     * @return 群组ID列表
     */
    @Query("SELECT gm.groupId FROM GroupMember gm WHERE gm.userId = :userId")
    List<Long> findGroupIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据群组ID，查找所有群成员的记录
     * @param groupId 群组ID
     * @return 群成员列表
     */
    List<GroupMember> findByGroupId(Long groupId);

    void deleteByGroupIdAndUserId(Long groupId, Long userId);
}
