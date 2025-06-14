package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GroupAgree")
@Getter
@Setter
@NoArgsConstructor
public class GroupAgree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Agree_ID;
    
    private Boolean status;
}