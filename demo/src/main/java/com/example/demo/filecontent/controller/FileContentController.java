package com.example.demo.filecontent.controller;

import com.example.demo.common.SecurityUtil;
import com.example.demo.filecontent.dto.request.SaveFileContentRequestDto;
import com.example.demo.filecontent.dto.response.FileContentResponseDto;
import com.example.demo.filecontent.service.FileContentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file-contents")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class FileContentController {

    private final FileContentService fileContentService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<FileContentResponseDto> saveFileContent(
            @RequestBody SaveFileContentRequestDto requestDto,
            Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        FileContentResponseDto response = fileContentService.saveFileContent(requestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<FileContentResponseDto> getFileContent(
            @PathVariable Long fileId,
            Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        FileContentResponseDto response = fileContentService.getFileContent(fileId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/file/{fileId}/version/{version}")
    public ResponseEntity<FileContentResponseDto> getFileContentByVersion(
            @PathVariable Long fileId,
            @PathVariable Integer version,
            Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        FileContentResponseDto response = fileContentService.getFileContentByVersion(fileId, version, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/file/{fileId}/history")
    public ResponseEntity<List<FileContentResponseDto>> getFileContentHistory(
            @PathVariable Long fileId,
            Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        List<FileContentResponseDto> response = fileContentService.getFileContentHistory(fileId, userId);
        return ResponseEntity.ok(response);
    }
}