package com.example.demo.file.service;

import com.example.demo.file.dto.request.CreateFileRequestDto;
import com.example.demo.file.dto.request.UpdateFileNameRequestDto;
import com.example.demo.file.dto.response.FileResponseDto;
import com.example.demo.file.dto.response.FileTreeNodeDto;

import java.util.List;

public interface FileService {
    // 생성
    FileResponseDto createFile(CreateFileRequestDto requestDto, Long userId);

    // 조회
    FileResponseDto getFile(Long fileId, Long userId);

    // 트리 조회
    List<FileTreeNodeDto> getFileTree(Long projectId, Long userId);

    // 수정
    FileResponseDto updateFileName(Long fileId, UpdateFileNameRequestDto requestDto, Long userId);

    // 삭제
    void deleteFile(Long fileId, Long userId);
}

