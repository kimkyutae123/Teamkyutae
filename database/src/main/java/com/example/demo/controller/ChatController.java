package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.ChatRoom;
import com.example.demo.model.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // 채팅 메시지 전송
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Integer roomId,
            @RequestParam Integer userId,
            @RequestBody String content) {
        return chatRoomRepository.findById(roomId)
                .flatMap(room -> userRepository.findById(userId)
                        .map(user -> {
                            ChatMessage message = new ChatMessage();
                            message.setChatRoom(room);
                            message.setUser(user);
                            message.setContent(content);
                            return ResponseEntity.ok(chatMessageRepository.save(message));
                        }))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 채팅방의 메시지 목록 조회
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Integer roomId) {
        if (chatRoomRepository.existsById(roomId)) {
            List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(roomId);
            return ResponseEntity.ok(messages);
        }
        return ResponseEntity.notFound().build();
    }
    
    // 그룹의 채팅방 조회
    @GetMapping("/groups/{groupId}/room")
    public ResponseEntity<?> getGroupChatRoom(@PathVariable Integer groupId) {
        ChatRoom chatRoom = chatRoomRepository.findByGroupId(groupId);
        if (chatRoom != null) {
            return ResponseEntity.ok(chatRoom);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@RequestBody ChatRoom room) {
        try {
            ChatRoom savedRoom = chatRoomRepository.save(room);
            return ResponseEntity.ok(savedRoom);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<?> getAllRooms() {
        try {
            List<ChatRoom> rooms = chatRoomRepository.findAll();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/rooms/{roomId}/users/{userId}")
    public ResponseEntity<?> addUserToRoom(@PathVariable Integer roomId, @PathVariable Integer userId) {
        try {
            return chatRoomRepository.findById(roomId)
                    .flatMap(room -> userRepository.findById(userId)
                            .map(user -> {
                                room.getUsers().add(user);
                                return ResponseEntity.ok(chatRoomRepository.save(room));
                            }))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 