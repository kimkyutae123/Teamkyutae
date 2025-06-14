package com.example.project_1;

public class ChatMessage
{
    private String message;
    private boolean isMine;

    public ChatMessage(String message, boolean isMine)
    {
        this.message = message;
        this.isMine = isMine;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean isMine()
    {
        return isMine;
    }
}
