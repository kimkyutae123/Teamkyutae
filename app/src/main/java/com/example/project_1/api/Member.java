package com.example.project_1.api;

public class Member {
    private String userName;
    private String userAge;
    private Long userId;  // 서버에서 아이디 받으면 여기에 저장

    public Member(String userName, String userAge) {
        this.userName = userName;
        this.userAge = userAge;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserAge() {
        return userAge;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
