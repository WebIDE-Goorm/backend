package com.example.demo.file.dto.response;

import com.example.demo.file.entity.FileEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileResponseDto {
    private Long id;
    private Long projectId;
    private Long parentId;
    private String name;
    private FileEntity.FileType type;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

