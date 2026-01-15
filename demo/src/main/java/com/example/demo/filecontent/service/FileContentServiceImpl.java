package com.example.demo.filecontent.service;

import com.example.demo.file.entity.FileEntity;
import com.example.demo.file.repository.FileRepository;
import com.example.demo.filecontent.dto.request.SaveFileContentRequestDto;
import com.example.demo.filecontent.dto.response.FileContentResponseDto;
import com.example.demo.filecontent.entity.FileContent;
import com.example.demo.filecontent.repository.FileContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileContentServiceImpl implements FileContentService {

    private final FileContentRepository fileContentRepository;
    private final FileRepository fileRepository;

    // 저장
    @Override
    @Transactional
    public FileContentResponseDto saveFileContent(SaveFileContentRequestDto requestDto) {
        // File 존재 여부 및 타입 검증
        FileEntity file = fileRepository.findByIdAndIsDeletedFalse(requestDto.getFileId())
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. (fileId: " + requestDto.getFileId() + ")"));
        
        if (file.getType() != FileEntity.FileType.FILE) {
            throw new RuntimeException("폴더에는 내용을 저장할 수 없습니다. (fileId: " + requestDto.getFileId() + ")");
        }

        Integer nextVersion = fileContentRepository.findFirstByFileIdOrderByVersionDesc(requestDto.getFileId())
            .map(fileContent -> fileContent.getVersion() + 1)
            .orElse(1);

        FileContent fileContent = FileContent.create(
            requestDto.getFileId(),
            requestDto.getContent(),
            nextVersion
        );

        FileContent savedFileContent = fileContentRepository.save(fileContent);

        return toResponseDto(savedFileContent);
    }

    // 조회
    @Override
    public FileContentResponseDto getFileContent(Long fileId) {
        FileEntity file = fileRepository.findByIdAndIsDeletedFalse(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. (fileId: " + fileId + ")"));

        if (file.getType() != FileEntity.FileType.FILE) {
            throw new RuntimeException("폴더는 내용을 조회할 수 없습니다. (fileId: " + fileId + ")");
        }

        FileContent fileContent = fileContentRepository.findFirstByFileIdOrderByVersionDesc(fileId)
            .orElseThrow(() -> new RuntimeException("파일 내용을 찾을 수 없습니다. (fileId: " + fileId + ")"));

        return toResponseDto(fileContent);
    }

    // 버전으로 조회
    @Override
    public FileContentResponseDto getFileContentByVersion(Long fileId, Integer version) {
        FileContent fileContent = fileContentRepository.findByFileIdAndVersion(fileId, version)
            .orElseThrow(() -> new RuntimeException(
                "파일 내용을 찾을 수 없습니다. (fileId: " + fileId + ", version: " + version + ")"
            ));

        return toResponseDto(fileContent);
    }

    // 기록 조회
    @Override
    public List<FileContentResponseDto> getFileContentHistory(Long fileId) {
        List<FileContent> fileContents = fileContentRepository.findByFileIdOrderByVersionDesc(fileId);

        return fileContents.stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList());
    }

    private FileContentResponseDto toResponseDto(FileContent fileContent) {
        return FileContentResponseDto.builder()
            .id(fileContent.getId())
            .fileId(fileContent.getFileId())
            .content(fileContent.getContent())
            .version(fileContent.getVersion())
            .updatedAt(fileContent.getUpdatedAt())
            .build();
    }
}