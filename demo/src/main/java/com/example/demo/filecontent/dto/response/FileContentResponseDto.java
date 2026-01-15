package com.example.demo.filecontent.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileContentResponseDto {
    private Long id;
    private Long fileId;
    private String content;
    private Integer version;
    private LocalDateTime updatedAt;
}

