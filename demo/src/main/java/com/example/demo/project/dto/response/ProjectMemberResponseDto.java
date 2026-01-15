package com.example.demo.project.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectMemberResponseDto {
    private Long id;
    private Long projectId;
    private Long userId;
    private LocalDateTime createdAt;
}
