package com.footballmania.waslogin.entity;

import jakarta.persistence.*;

import java.util.UUID;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@RequiredArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="uuid", unique = true)
    private String uuid;

    @Column(name="name", nullable = false)
    private String name;
    
    @Column(name="email", unique = true, nullable = false)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Builder
    public User(String name, String email, String nickname, String password, String contact) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
    
    @PrePersist
    public void autofill() {
        this.setUuid(UUID.randomUUID().toString());
    }
}
