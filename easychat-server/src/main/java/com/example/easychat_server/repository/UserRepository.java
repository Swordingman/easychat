package com.example.easychat_server.repository;

import com.example.easychat_server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEasychatId(String easychatId);

    List<User> findByEasychatIdContainingOrNicknameContainingAndIdNot(String easychatId, String nickname, Long selfId);
}