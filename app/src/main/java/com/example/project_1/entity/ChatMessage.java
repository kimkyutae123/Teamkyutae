package com.example.project_1.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private String message;
    private boolean fromMe;

    public ChatMessage(String message, boolean fromMe) {
        this.message = message;
        this.fromMe = fromMe;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFromMe() {
        return fromMe;
    }
} 