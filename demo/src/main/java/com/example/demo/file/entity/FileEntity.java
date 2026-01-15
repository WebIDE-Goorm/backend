package com.example.demo.file.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column
    private Long parentId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType type;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum FileType {
        FILE, FOLDER
    }

    // 파일/폴더 생성
    public static FileEntity create(Long projectId, Long parentId, String name, FileType type) {
        FileEntity file = new FileEntity();
        file.projectId = projectId;
        file.parentId = parentId; // null = 루트
        file.name = name;
        file.type = type;
        file.isDeleted = false;
        return file;
    }

    public void updateName(String name) {
        this.name = name;
        onUpdated();
    }

    public void delete() {
        this.isDeleted = true;
        onUpdated();
    }

    // 복원
    public void restore() {
        this.isDeleted = false;
        onUpdated();
    }

    @PrePersist
    protected void onCreated() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    @PreUpdate
    protected void onUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
}

