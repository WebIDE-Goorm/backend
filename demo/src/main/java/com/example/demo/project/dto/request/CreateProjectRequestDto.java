package com.example.demo.project.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateProjectRequestDto {
    private String name;
    private String description;
    //private Long ownerId;
}

