package com.example.easychat_server.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_group_member")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long groupId;
    private Long userId;
    private String memberNickname;
    private String role;
    @CreationTimestamp
    private LocalDateTime joinTime;
}