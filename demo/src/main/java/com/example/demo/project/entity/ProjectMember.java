package com.example.demo.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long userId;

    // @Column(nullable = false)
    // private String role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static ProjectMember create(Long projectId, Long userId) {
        ProjectMember member = new ProjectMember();
        member.projectId = projectId;
        member.userId = userId;

        return member;
    }

    @PrePersist
    protected void onCreated() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
    }

    // public void changeRole(String role) {
    //     this.role = role;
    // }

}

