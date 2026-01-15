package com.example.demo.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    // 차후 확장
    // @Column(nullable = false)
    // private Long ownerId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(unique = true, length = 16)
    private String inviteCode;

    public static Project create(String name, String description) {
        Project project = new Project();
        project.name = name;
        project.description = description;

        return project;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        onUpdated();
    }

    @PrePersist
    protected void onCreated() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    protected void onUpdated() {
        LocalDateTime now = LocalDateTime.now();
        this.updatedAt = now;
    }

    // ------------------------------------------------------------
    // 초대 코드
    // ------------------------------------------------------------
    public void generateInviteCode() {
        int inviteCodeLength = 8; // 기본 초대 코드 길이
        this.inviteCode = generateRandomCode(inviteCodeLength);
    }

    public void generateInviteCode(int length) {
        this.inviteCode = generateRandomCode(length);
    }

    public void regenerateInviteCode() {
        generateInviteCode();
        onUpdated();
    }

    private String generateRandomCode(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }

}

