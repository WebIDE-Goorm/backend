package com.example.demo.project.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectResponseDto {
    private Long id;
    private String name;
    private String description;
    //private Long ownerId;
    private String inviteCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

