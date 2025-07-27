package com.example.easychat_server.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_contact")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id_a", nullable = false)
    private Long userIdA;

    @Column(name = "user_id_b", nullable = false)
    private Long userIdB;

    // 可以在这里加一个 status 字段，如 "FRIENDS", "BLOCKED" 等

    @Column(name = "create_time", updatable = false)
    @CreationTimestamp
    private LocalDateTime createTime;
}
