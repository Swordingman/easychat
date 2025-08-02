package com.example.easychat_server.repository;

import com.example.easychat_server.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) OR (m.senderId = :userId2 AND m.receiverId = :userId1) ORDER BY m.createTime ASC")
    List<Message> findConversationMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    Optional<Message> findTopBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreateTimeDesc(
            Long senderId1, Long receiverId1, Long senderId2, Long receiverId2);

    List<Message> findByReceiverGroupIdOrderByCreateTimeAsc(Long groupId);
}
