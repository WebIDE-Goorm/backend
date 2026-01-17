package com.example.demo.filecontent.service;

import com.example.demo.common.CustomException;
import com.example.demo.common.ErrorMessage;
import com.example.demo.file.entity.FileEntity;
import com.example.demo.file.repository.FileRepository;
import com.example.demo.filecontent.dto.request.SaveFileContentRequestDto;
import com.example.demo.filecontent.dto.response.FileContentResponseDto;
import com.example.demo.filecontent.entity.FileContent;
import com.example.demo.filecontent.repository.FileContentRepository;
import com.example.demo.project.service.ProjectMemberService;
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
    private final ProjectMemberService projectMemberService;

    // 저장
    @Override
    @Transactional
    public FileContentResponseDto saveFileContent(SaveFileContentRequestDto requestDto, Long userId) {
        // File 존재 여부 및 타입 검증
        FileEntity file = fileRepository.findByIdAndIsDeletedFalse(requestDto.getFileId())
            .orElseThrow(() -> new CustomException(ErrorMessage.FILE_NOT_FOUND));
        
        if (file.getType() != FileEntity.FileType.FILE) {
            throw new CustomException(ErrorMessage.CANNOT_SAVE_TO_FOLDER);
        }

        projectMemberService.validateProjectMember(file.getProjectId(), userId);

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
    public FileContentResponseDto getFileContent(Long fileId, Long userId) {
        FileEntity file = fileRepository.findByIdAndIsDeletedFalse(fileId)
            .orElseThrow(() -> new CustomException(ErrorMessage.FILE_NOT_FOUND));

        if (file.getType() != FileEntity.FileType.FILE) {
            throw new CustomException(ErrorMessage.CANNOT_READ_FOLDER_CONTENT);
        }

        projectMemberService.validateProjectMember(file.getProjectId(), userId);

        FileContent fileContent = fileContentRepository.findFirstByFileIdOrderByVersionDesc(fileId)
            .orElseThrow(() -> new CustomException(ErrorMessage.FILE_CONTENT_NOT_FOUND));

        return toResponseDto(fileContent);
    }

    // 버전으로 조회
    @Override
    public FileContentResponseDto getFileContentByVersion(Long fileId, Integer version, Long userId) {
        FileEntity file = fileRepository.findByIdAndIsDeletedFalse(fileId)
            .orElseThrow(() -> new CustomException(ErrorMessage.FILE_NOT_FOUND));

        projectMemberService.validateProjectMember(file.getProjectId(), userId);

        FileContent fileContent = fileContentRepository.findByFileIdAndVersion(fileId, version)
            .orElseThrow(() -> new CustomException(ErrorMessage.FILE_CONTENT_NOT_FOUND));

        return toResponseDto(fileContent);
    }

    // 기록 조회
    @Override
    public List<FileContentResponseDto> getFileContentHistory(Long fileId, Long userId) {
        FileEntity file = fileRepository.findByIdAndIsDeletedFalse(fileId)
            .orElseThrow(() -> new CustomException(ErrorMessage.FILE_NOT_FOUND));

        projectMemberService.validateProjectMember(file.getProjectId(), userId);

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