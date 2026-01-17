package com.example.demo.filecontent.service;

import com.example.demo.filecontent.dto.request.SaveFileContentRequestDto;
import com.example.demo.filecontent.dto.response.FileContentResponseDto;

import java.util.List;

public interface FileContentService {
    // 저장
    FileContentResponseDto saveFileContent(SaveFileContentRequestDto requestDto, Long userId);

    // 조회
    FileContentResponseDto getFileContent(Long fileId, Long userId);

    // 버전별 조회
    FileContentResponseDto getFileContentByVersion(Long fileId, Integer version, Long userId);

    // 히스토리 조회
    List<FileContentResponseDto> getFileContentHistory(Long fileId, Long userId);
}

