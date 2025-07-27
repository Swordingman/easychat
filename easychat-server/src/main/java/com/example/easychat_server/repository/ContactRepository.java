package com.example.easychat_server.repository;

import com.example.easychat_server.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    // 根据用户ID查找所有好友关系
    List<Contact> findByUserIdAOrUserIdB(Long userIdA, Long userIdB);
}