package com.example.demo.controller;

import com.example.demo.model.Group;
import com.example.demo.model.ChatRoom;
import com.example.demo.model.User;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupController {
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    
    // 그룹 생성
    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Group group) {
        try {
            // 채팅방 생성
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setGroup(group);
            
            // 그룹 저장
            Group savedGroup = groupRepository.save(group);
            
            // 채팅방 저장
            chatRoomRepository.save(chatRoom);
            
            return ResponseEntity.ok(savedGroup);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // 모든 그룹 조회
    @GetMapping
    public ResponseEntity<?> getAllGroups() {
        try {
            List<Group> groups = groupRepository.findAll();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // 특정 그룹 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getGroup(@PathVariable Integer id) {
        try {
            return groupRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // 그룹 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Integer id) {
        try {
            if (!groupRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            groupRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "그룹이 성공적으로 삭제되었습니다"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // 그룹에 사용자 추가
    @PostMapping("/{groupId}/users/{userId}")
    public ResponseEntity<?> addUserToGroup(@PathVariable Integer groupId, @PathVariable Integer userId) {
        try {
            return groupRepository.findById(groupId)
                    .flatMap(group -> userRepository.findById(userId)
                            .map(user -> {
                                group.getUsers().add(user);
                                return ResponseEntity.ok(groupRepository.save(group));
                            }))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // 그룹에서 사용자 제거
    @DeleteMapping("/{groupId}/users/{userId}")
    public ResponseEntity<?> removeUserFromGroup(@PathVariable Integer groupId, @PathVariable Integer userId) {
        try {
            return groupRepository.findById(groupId)
                    .flatMap(group -> userRepository.findById(userId)
                            .map(user -> {
                                group.getUsers().remove(user);
                                return ResponseEntity.ok(groupRepository.save(group));
                            }))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserGroups(@PathVariable Integer userId) {
        try {
            return userRepository.findById(userId)
                    .map(user -> ResponseEntity.ok(user.getGroups()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 