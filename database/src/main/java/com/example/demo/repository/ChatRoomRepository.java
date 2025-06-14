package com.example.demo.repository;

import com.example.demo.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    ChatRoom findByGroupId(Integer groupId);
} 