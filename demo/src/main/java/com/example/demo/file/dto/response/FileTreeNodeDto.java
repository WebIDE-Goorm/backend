package com.example.demo.file.dto.response;

import com.example.demo.file.entity.FileEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class FileTreeNodeDto {
    private Long id;
    private Long projectId;
    private Long parentId;
    private String name;
    private FileEntity.FileType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FileTreeNodeDto> children;
}

