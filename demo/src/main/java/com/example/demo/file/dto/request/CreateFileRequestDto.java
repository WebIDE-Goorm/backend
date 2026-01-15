package com.example.demo.file.dto.request;

import com.example.demo.file.entity.FileEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateFileRequestDto {
    private Long projectId;
    private Long parentId;
    private String name;
    private FileEntity.FileType type;
}