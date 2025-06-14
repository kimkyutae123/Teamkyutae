package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 6)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean agree = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Location location;

    @ManyToMany(mappedBy = "users")
    private Set<Group> groups;

    @OneToMany(mappedBy = "user")
    private Set<ChatMessage> chatMessages;
} 