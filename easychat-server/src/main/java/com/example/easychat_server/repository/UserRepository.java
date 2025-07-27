package com.example.easychat_server.repository;

import com.example.easychat_server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Spring Data JPA 的神奇之处：
     * 你只需要按照规范定义方法名，它就会自动为你生成 SQL 实现。
     * "findByUsername" 会被自动解析为: SELECT * FROM t_user WHERE username = ?
     *
     * @param username 用户名
     * @return 返回一个 Optional<User>，可以优雅地处理用户可能不存在的情况，避免空指针异常。
     */
    Optional<User> findByUsername(String username);

}