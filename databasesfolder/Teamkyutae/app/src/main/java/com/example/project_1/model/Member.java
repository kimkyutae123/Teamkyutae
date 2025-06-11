package com.example.project_1.model;

import com.google.gson.annotations.SerializedName;

public class Member {
    @SerializedName("user_id")
    private Integer userId;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("email")
    private String email;

    public Member() {}

    public Member(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
} 