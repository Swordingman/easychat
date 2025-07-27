package com.example.easychat_server.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

// Lombok 注解，自动生成 Getter, Setter, toString, 等方法
@Data
// JPA 注解，表示这是一个实体类，将映射到数据库的表
@Entity
// 指定映射的表名，如果省略，默认为类名的小写 user
@Table(name = "t_user")
public class User {

    // @Id: 声明这是主键
    // @GeneratedValue: 指定主键的生成策略，IDENTITY 表示自增长
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column: 用于定义列的属性
    // unique = true: 该列的值必须唯一，对应数据库的 UNIQUE 约束
    // nullable = false: 该列不能为空，对应数据库的 NOT NULL 约束
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    // length = 512: 为头像URL预留足够长的空间
    @Column(length = 512)
    private String avatar;

    // @CreationTimestamp: 当实体被创建时，自动填充当前时间戳
    @Column(name = "create_time", updatable = false)
    @CreationTimestamp
    private LocalDateTime createTime;

    // JPA 需要一个无参构造函数
    public User() {
    }

    // 我们可以添加一个方便的构造函数用于注册
    public User(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        // 默认头像
        this.avatar = "https://example.com/default-avatar.png";
    }
}
