package com.example.myapplication;

public class GroupMember {
    private String name;
    private boolean consent;

    public GroupMember(String name, boolean consent) {
        this.name = name;
        this.consent = consent;
    }

    public String getName() {
        return name;
    }

    public boolean hasConsented() {
        return consent;
    }
}
