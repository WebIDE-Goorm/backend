package com.example.demo.project.service;

import com.example.demo.common.CustomException;
import com.example.demo.common.ErrorMessage;
import com.example.demo.file.entity.FileEntity;
import com.example.demo.file.repository.FileRepository;
import com.example.demo.filecontent.repository.FileContentRepository;
import com.example.demo.project.dto.request.AddProjectMemberRequestDto;
import com.example.demo.project.dto.request.CreateProjectRequestDto;
import com.example.demo.project.dto.response.ProjectResponseDto;
import com.example.demo.project.entity.Project;
import com.example.demo.project.repository.ProjectMemberRepository;
import com.example.demo.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMemberService projectMemberService;
    private final FileRepository fileRepository;
    private final FileContentRepository fileContentRepository;

    @Override
    @Transactional
    public ProjectResponseDto createProject(CreateProjectRequestDto requestDto, Long userId) {
        Project project = Project.create(requestDto.getName(), requestDto.getDescription());

        ensureUniqueInviteCode(project);
        
        Project savedProject = projectRepository.save(project);

        // 프로젝트 생성 시 생성자를 멤버로 자동 추가
        AddProjectMemberRequestDto addMemberDto = new AddProjectMemberRequestDto();
        addMemberDto.setProjectId(savedProject.getId());
        addMemberDto.setUserId(userId);
        projectMemberService.addMember(addMemberDto);

        // 프로젝트 생성 시 루트 디렉토리 자동 생성
        FileEntity rootFolder = FileEntity.create(
            savedProject.getId(),
            null, // parentId = null (루트)
            savedProject.getName(),
            FileEntity.FileType.FOLDER
        );
        fileRepository.save(rootFolder);

        return toResponseDto(savedProject);
    }

    @Override
    public ProjectResponseDto getProject(Long projectId, Long userId) {
        projectMemberService.validateProjectMember(projectId, userId);
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new CustomException(ErrorMessage.PROJECT_NOT_FOUND));

        return toResponseDto(project);
    }

    @Override
    public List<ProjectResponseDto> getAllProjects() {
        List<Project> projects = projectRepository.findAll();

        return projects.stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProjectResponseDto updateProject(Long projectId, CreateProjectRequestDto requestDto, Long userId) {
        projectMemberService.validateProjectMember(projectId, userId);
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new CustomException(ErrorMessage.PROJECT_NOT_FOUND));

        project.update(requestDto.getName(), requestDto.getDescription());

        Project updatedProject = projectRepository.save(project);

        return toResponseDto(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        // 프로젝트 멤버 여부 검증
        projectMemberService.validateProjectMember(projectId, userId);
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new CustomException(ErrorMessage.PROJECT_NOT_FOUND));

        // 연관된 FileContent 삭제 (프로젝트의 모든 파일의 FileContent)
        List<FileEntity> projectFiles = fileRepository.findByProjectIdAndIsDeletedFalse(projectId);
        for (FileEntity file : projectFiles) {
            fileContentRepository.deleteAll(fileContentRepository.findByFileIdOrderByVersionDesc(file.getId()));
        }

        // 연관된 File 삭제
        fileRepository.deleteAll(projectFiles);

        // 연관된 ProjectMember 삭제
        projectMemberRepository.deleteAll(projectMemberRepository.findByProjectId(projectId));

        // 프로젝트 삭제
        projectRepository.delete(project);
    }

    private ProjectResponseDto toResponseDto(Project project) {
        return ProjectResponseDto.builder()
            .id(project.getId())
            .name(project.getName())
            .description(project.getDescription())
            //.ownerId(project.getOwnerId()) // 차후 확장
            .inviteCode(project.getInviteCode())
            .createdAt(project.getCreatedAt())
            .updatedAt(project.getUpdatedAt())
            .build();
    }

    @Override
    public ProjectResponseDto getProjectByInviteCode(String inviteCode) {
        Project project = projectRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new CustomException(ErrorMessage.INVALID_INVITE_CODE));

        return toResponseDto(project);
    }

    @Override
    public List<ProjectResponseDto> getProjectsByUserId(Long userId) {
        List<Long> projectIds = projectMemberService.getUserProjects(userId);
        
        List<Project> projects = projectRepository.findAllById(projectIds);
        
        return projects.stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList());
    }

    // 고유 초대 코드 생성
    private void ensureUniqueInviteCode(Project project) {
        int maxRetries = 5; // 재시도 횟수 설정
        int retryCount = 0;
        int finalRetryCodeLength = 12; // 재시도 후 사용할 코드 길이

        while (retryCount < maxRetries) {
            project.generateInviteCode();
            if (!projectRepository.existsByInviteCode(project.getInviteCode())) {
                return;
            }
            retryCount++;
        }

        // 재시도 후에도 충돌 시 더 긴 코드 사용
        project.generateInviteCode(finalRetryCodeLength);
        while (projectRepository.existsByInviteCode(project.getInviteCode())) {
            project.generateInviteCode(finalRetryCodeLength);
        }
    }


}

