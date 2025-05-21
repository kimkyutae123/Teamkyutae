package com.example.demo.controller;


import com.example.demo.model.Member;
import com.example.demo.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMappding("/api")
public class MemberController {

    @Autowired
    private MemberRepository repository;

    @PostMapping("/save")
    public ResponseEntity<String> saveMember(@RequestBody Member member) {
        repository.save(member);
        return ResponseEntity.ok("저장 완료");
    }
}
