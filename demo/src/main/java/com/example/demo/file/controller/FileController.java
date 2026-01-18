package com.example.demo.file.controller;

import com.example.demo.common.SecurityUtil;
import com.example.demo.file.dto.request.CreateFileRequestDto;
import com.example.demo.file.dto.request.UpdateFileNameRequestDto;
import com.example.demo.file.dto.response.FileResponseDto;
import com.example.demo.file.dto.response.FileTreeNodeDto;
import com.example.demo.file.service.FileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class FileController {

    private final FileService fileService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<FileResponseDto> createFile(
            @RequestBody CreateFileRequestDto requestDto,
            Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        FileResponseDto response = fileService.createFile(requestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponseDto> getFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        FileResponseDto response = fileService.getFile(fileId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}/tree")
    public ResponseEntity<List<FileTreeNodeDto>> getFileTree(
            @PathVariable Long projectId,
            Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        List<FileTreeNodeDto> response = fileService.getFileTree(projectId, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{fileId}/name")
    public ResponseEntity<FileResponseDto> updateFileName(
            @PathVariable Long fileId,
            @RequestBody UpdateFileNameRequestDto requestDto,
            Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        FileResponseDto response = fileService.updateFileName(fileId, requestDto, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        fileService.deleteFile(fileId, userId);
        return ResponseEntity.noContent().build();
    }
}