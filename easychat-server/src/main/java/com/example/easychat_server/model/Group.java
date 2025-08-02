package com.example.easychat_server.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_group")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String groupName;
    private Long ownerId;
    private String avatar;
    private String announcement;
    @CreationTimestamp
    private LocalDateTime createTime;
}


