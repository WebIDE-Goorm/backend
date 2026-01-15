package com.example.demo.filecontent.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SaveFileContentRequestDto {
    private Long fileId;
    private String content;
}

