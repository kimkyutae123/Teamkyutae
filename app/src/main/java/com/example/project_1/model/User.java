package com.example.teamkyutae.model;

public class User {
    private int id;
    private String name;
    private boolean agree;

    public User(int id, String name, boolean agree) {
        this.id = id;
        this.name = name;
        this.agree = agree;
    }

    public User(String name) {
        this.name = name;
        this.agree = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
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