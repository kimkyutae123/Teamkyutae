package com.example.project_1.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Group {
    private Long groupId;
    private String groupName;
    private Member user;
} 