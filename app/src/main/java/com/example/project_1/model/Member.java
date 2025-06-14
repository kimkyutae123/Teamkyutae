package com.example.project_1.model;

import com.google.gson.annotations.SerializedName;

public class Member {
    @SerializedName("id")
    private Integer id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("agree")
    private boolean agree;

    public Member() {}

    public Member(String name) {
        this.name = name;
        this.agree = false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAgree() {
        return agree;
    }

    public void setAgree(boolean agree) {
        this.agree = agree;
    }
} 