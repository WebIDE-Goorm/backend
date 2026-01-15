package com.example.demo.file.service;

import com.example.demo.file.dto.request.CreateFileRequestDto;
import com.example.demo.file.dto.request.UpdateFileNameRequestDto;
import com.example.demo.file.dto.response.FileResponseDto;
import com.example.demo.file.dto.response.FileTreeNodeDto;

import java.util.List;

public interface FileService {
    // 생성
    FileResponseDto createFile(CreateFileRequestDto requestDto);

    // 조회
    FileResponseDto getFile(Long fileId);

    // 트리 조회
    List<FileTreeNodeDto> getFileTree(Long projectId);

    // 수정
    FileResponseDto updateFileName(Long fileId, UpdateFileNameRequestDto requestDto);

    // 삭제
    void deleteFile(Long fileId);
}

