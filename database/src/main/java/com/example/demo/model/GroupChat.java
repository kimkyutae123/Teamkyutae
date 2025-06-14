package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GroupChat")
@Getter
@Setter
@NoArgsConstructor
public class GroupChat {
    @Id
    private Integer Room_id;
    
    @Column(nullable = false)
    private Integer GroupRoom_user;
    
    private String Message_text;
    
    @ManyToOne
    @JoinColumn(name = "send_id")
    private Member sender;
    
    @ManyToOne
    @JoinColumn(name = "Group_id")
    private Group group;
}