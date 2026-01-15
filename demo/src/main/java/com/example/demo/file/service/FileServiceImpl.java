package com.example.demo.file.service;

import com.example.demo.file.dto.request.CreateFileRequestDto;
import com.example.demo.file.dto.request.UpdateFileNameRequestDto;
import com.example.demo.file.dto.response.FileResponseDto;
import com.example.demo.file.dto.response.FileTreeNodeDto;
import com.example.demo.file.entity.FileEntity;
import com.example.demo.file.repository.FileRepository;
import com.example.demo.filecontent.repository.FileContentRepository;
import com.example.demo.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final ProjectRepository projectRepository;
    private final FileContentRepository fileContentRepository;

    // 파일/폴더 생성
    @Override
    @Transactional
    public FileResponseDto createFile(CreateFileRequestDto requestDto) {
        // Project 존재 여부 검증
        projectRepository.findById(requestDto.getProjectId())
            .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다. (projectId: " + requestDto.getProjectId() + ")"));

        // 루트 디렉토리가 아니면 존재 여부 및 프로젝트 검증
        if (requestDto.getParentId() != null) {
            FileEntity parent = fileRepository.findByIdAndIsDeletedFalse(requestDto.getParentId())
                .orElseThrow(() -> new RuntimeException("부모 폴더를 찾을 수 없습니다. (parentId: " + requestDto.getParentId() + ")"));
            
            // 같은 프로젝트인지 확인
            if (!parent.getProjectId().equals(requestDto.getProjectId())) {
                throw new RuntimeException("부모 폴더가 다른 프로젝트에 속해 있습니다. (parentId: " + requestDto.getParentId() + ")");
            }
            
            // 부모가 폴더인지 확인
            if (parent.getType() != FileEntity.FileType.FOLDER) {
                throw new RuntimeException("부모는 폴더여야 합니다. (parentId: " + requestDto.getParentId() + ")");
            }
        }

        if (fileRepository.existsByProjectIdAndParentIdAndNameAndIsDeletedFalse(requestDto.getProjectId(),requestDto.getParentId(), requestDto.getName())) {
            throw new RuntimeException("같은 이름의 파일이 이미 존재합니다. (name: " + requestDto.getName() + ")");
        }

        FileEntity file = FileEntity.create(
            requestDto.getProjectId(),
            requestDto.getParentId(),
            requestDto.getName(),
            requestDto.getType()
        );

        FileEntity savedFile = fileRepository.save(file);

        return toResponseDto(savedFile);
    }

    @Override
    public FileResponseDto getFile(Long fileId) {
        FileEntity file = fileRepository.findByIdAndIsDeletedFalse(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. (id: " + fileId + ")"));

        return toResponseDto(file);
    }

    // 파일 트리 조회
    @Override
    public List<FileTreeNodeDto> getFileTree(Long projectId) {
        List<FileEntity> allFiles = fileRepository.findByProjectIdAndIsDeletedFalse(projectId);

        Map<Long, FileTreeNodeDto> fileMap = allFiles.stream()
            .map(this::toTreeNodeDto)
            .collect(Collectors.toMap(
                FileTreeNodeDto::getId,
                dto -> dto
            ));

        List<FileTreeNodeDto> rootNodes = new ArrayList<>();

        for (FileEntity file : allFiles) {
            FileTreeNodeDto node = fileMap.get(file.getId());
            
            if (file.getParentId() == null) {
                rootNodes.add(node);
            } else {
                FileTreeNodeDto parent = fileMap.get(file.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }

        return rootNodes;
    }


    // 파일명 수정
    @Override
    @Transactional
    public FileResponseDto updateFileName(Long fileId, UpdateFileNameRequestDto requestDto) {
        FileEntity file = fileRepository.findByIdAndIsDeletedFalse(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. (id: " + fileId + ")"));

        // 자기 자신을 제외하고 중복 체크
        if (fileRepository.existsByProjectIdAndParentIdAndNameAndIdNotAndIsDeletedFalse(
            file.getProjectId(),
            file.getParentId(),
            requestDto.getName(),
            fileId
        )) {
            throw new RuntimeException("같은 이름의 파일이 이미 존재합니다. (name: " + requestDto.getName() + ")");
        }

        file.updateName(requestDto.getName());

        FileEntity updatedFile = fileRepository.save(file);

        return toResponseDto(updatedFile);
    }

    // 삭제
    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        FileEntity file = fileRepository.findByIdAndIsDeletedFalse(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. (id: " + fileId + ")"));

        // 폴더인 경우 하위 파일/폴더도 재귀 삭제 진행
        if (file.getType() == FileEntity.FileType.FOLDER) {
            deleteChildrenRecursively(fileId);
        }

        // 연관 FileContent 삭제
        fileContentRepository.deleteAll(fileContentRepository.findByFileIdOrderByVersionDesc(fileId));

        // 파일 삭제
        file.delete();
        fileRepository.save(file);
    }

    // 하위 파일/폴더 재귀적 삭제
    private void deleteChildrenRecursively(Long parentId) {
        List<FileEntity> children = fileRepository.findByParentIdAndIsDeletedFalse(parentId);
        
        for (FileEntity child : children) {
            // 폴더인 경우 재귀 하위 삭제 진행
            if (child.getType() == FileEntity.FileType.FOLDER) {
                deleteChildrenRecursively(child.getId());
            }
            
            // 연관 FileContent 삭제
            fileContentRepository.deleteAll(fileContentRepository.findByFileIdOrderByVersionDesc(child.getId()));
            
            // 파일 삭제
            child.delete();
            fileRepository.save(child);
        }
    }

    private FileResponseDto toResponseDto(FileEntity file) {
        return FileResponseDto.builder()
            .id(file.getId())
            .projectId(file.getProjectId())
            .parentId(file.getParentId())
            .name(file.getName())
            .type(file.getType())
            .isDeleted(file.getIsDeleted())
            .createdAt(file.getCreatedAt())
            .updatedAt(file.getUpdatedAt())
            .build();
    }

    private FileTreeNodeDto toTreeNodeDto(FileEntity file) {
        return FileTreeNodeDto.builder()
            .id(file.getId())
            .projectId(file.getProjectId())
            .parentId(file.getParentId())
            .name(file.getName())
            .type(file.getType())
            .createdAt(file.getCreatedAt())
            .updatedAt(file.getUpdatedAt())
            .children(new ArrayList<>())
            .build();
    }
}