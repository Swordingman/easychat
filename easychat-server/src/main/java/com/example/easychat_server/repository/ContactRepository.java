package com.example.easychat_server.repository;

import com.example.easychat_server.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    // 根据用户ID查找所有好友关系
    List<Contact> findByUserIdAOrUserIdB(Long userIdA, Long userIdB);

    boolean existsByUserIdAAndUserIdBOrUserIdBAndUserIdA(Long id1, Long id2, Long id3, Long id4);

    /**
     * 查找两个用户之间，无论方向和状态，是否存在任何关系记录
     * 用于防止重复发送请求或重复添加已经是好友的人
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 如果存在关系，则返回该关系
     */
    @Query("SELECT c FROM Contact c WHERE (c.userIdA = :userId1 AND c.userIdB = :userId2) OR (c.userIdA = :userId2 AND c.userIdB = :userId1)")
    Optional<Contact> findExistingRelation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 查询所有发给我的、并且状态是 PENDING 的请求
     * @param userIdB 接收者ID (也就是我的ID)
     * @param status 状态 "PENDING"
     * @return 请求列表
     */
    List<Contact> findByUserIdBAndStatus(Long userIdB, String status);

    /**
     * 查找一个用户所有状态为 ACCEPTED 的好友关系
     * @param userId 用户ID
     * @param status 状态 "ACCEPTED"
     * @return 好友关系列表
     */
    @Query("SELECT c FROM Contact c WHERE c.status = :status AND (c.userIdA = :userId OR c.userIdB = :userId)")
    List<Contact> findAllFriends(@Param("userId") Long userId, @Param("status") String status);
}