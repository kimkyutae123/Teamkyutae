package com.example.project_1.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChat {
    private Long roomId;
    private Integer groupRoomUser;
    private String messageText;
    private Member sender;
    private Group group;
} 