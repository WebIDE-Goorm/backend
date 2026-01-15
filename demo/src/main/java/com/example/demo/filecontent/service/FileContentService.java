package com.example.demo.filecontent.service;

import com.example.demo.filecontent.dto.request.SaveFileContentRequestDto;
import com.example.demo.filecontent.dto.response.FileContentResponseDto;

import java.util.List;

public interface FileContentService {
    FileContentResponseDto saveFileContent(SaveFileContentRequestDto requestDto);

    FileContentResponseDto getFileContent(Long fileId);

    FileContentResponseDto getFileContentByVersion(Long fileId, Integer version);

    List<FileContentResponseDto> getFileContentHistory(Long fileId);
}

