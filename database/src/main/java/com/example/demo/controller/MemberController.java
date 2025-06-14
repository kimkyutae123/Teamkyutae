package com.example.demo.controller;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Member;
import com.example.demo.repository.MemberRepository;

@RestController
@RequestMapping("/api/member")
public class MemberController {
    
    @Autowired
    private MemberRepository memberRepository;
    
    // 회원 등록
    @PostMapping("/register")
    public ResponseEntity<?> registerMember(@RequestBody Member member) {
        try {
            System.out.println("받은 데이터: " + member.toString());
            Member savedMember = memberRepository.save(member);
            System.out.println("저장된 데이터: " + savedMember.toString());
            return ResponseEntity.ok(savedMember);
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            return ResponseEntity.badRequest().body("회원 등록 실패: " + e.getMessage());
        }
    }

    // 회원 조회
    @GetMapping("/{userId}")
    public ResponseEntity<?> getMember(@PathVariable Integer userId) {
        try {
            Optional<Member> member = memberRepository.findById(userId);
            if(member.isPresent()) {
                return ResponseEntity.ok(member.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("회원 조회 실패: " + e.getMessage());
        }
    }
}