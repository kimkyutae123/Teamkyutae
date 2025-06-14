package com.example.project_1.model;

public class Group {
    private int id;
    private String name;
    private int leaderId;

    public Group(int id, String name, int leaderId) {
        this.id = id;
        this.name = name;
        this.leaderId = leaderId;
    }

    public Group(String name, int leaderId) {
        this.name = name;
        this.leaderId = leaderId;
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

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }
} 