package com.example.project_1.model;

public class ChatMessage {
    private int id;
    private int groupId;
    private int userId;
    private String content;
    private long timestamp;

    public ChatMessage(int id, int groupId, int userId, String content, long timestamp) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public ChatMessage(int groupId, int userId, String content) {
        this.groupId = groupId;
        this.userId = userId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 