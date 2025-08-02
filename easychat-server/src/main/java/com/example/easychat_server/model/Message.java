package com.example.easychat_server.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(nullable = false, length = 20)
    private String chatType; // "SINGLE", "GROUP"

    @Column(name = "receiver_group_id")
    private Long receiverGroupId;

    @Column(columnDefinition = "TEXT", nullable = false) // 使用 TEXT 类型以存储更长的消息
    private String content;

    @Column(name = "message_type", length = 20, nullable = false)
    private String messageType; // "TEXT", "IMAGE", "FILE" etc.

    @Column(name = "create_time", updatable = false)
    @CreationTimestamp
    private LocalDateTime createTime;
}
