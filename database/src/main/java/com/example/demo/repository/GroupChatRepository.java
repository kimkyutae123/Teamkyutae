package com.example.demo.repository;

import com.example.demo.model.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupChatRepository extends JpaRepository<GroupChat, Integer> {
    List<GroupChat> findByGroup_GroupId(Integer groupId);
}