package com.example.demo.repository;

import com.example.demo.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    List<Group> findByMember_UserId(Integer userId);
}