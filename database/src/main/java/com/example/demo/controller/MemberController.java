package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Member;
import com.example.demo.repository.MemberRepository;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
public class MemberController {
    
    @Autowired
    private MemberRepository memberRepository;
    
    @GetMapping
    public ResponseEntity<?> getAllMembers() {
        try {
            List<Member> members = memberRepository.findAll();
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "멤버 목록을 가져오는데 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getMember(@PathVariable Integer id) {
        try {
            Optional<Member> member = memberRepository.findById(id);
            if (member.isPresent()) {
                return ResponseEntity.ok(member.get());
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "멤버를 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "멤버 정보를 가져오는데 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member member) {
        try {
            if (member.getName() == null || member.getName().trim().isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "이름은 필수입니다");
                return ResponseEntity.badRequest().body(response);
            }
            Member savedMember = memberRepository.save(member);
            return ResponseEntity.ok(savedMember);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "멤버 생성에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Integer id) {
        try {
            if (memberRepository.existsById(id)) {
                memberRepository.deleteById(id);
                Map<String, String> response = new HashMap<>();
                response.put("message", "멤버가 성공적으로 삭제되었습니다");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "멤버를 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "멤버 삭제에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/agree")
    public ResponseEntity<?> updateAgreeStatus(@PathVariable Integer id, @RequestBody Map<String, Boolean> request) {
        try {
            Optional<Member> memberOpt = memberRepository.findById(id);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                member.setAgree(request.get("agree"));
                memberRepository.save(member);
                return ResponseEntity.ok(member);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "멤버를 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "동의 상태 업데이트에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}