package com.example.demo.project.service;

import com.example.demo.project.dto.request.AddProjectMemberRequestDto;
import com.example.demo.project.dto.response.ProjectMemberResponseDto;
import com.example.demo.project.entity.Project;
import com.example.demo.project.entity.ProjectMember;
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
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;

    // 프로젝트에 멤버 추가 (중복 체크 포함)
    @Override
    @Transactional
    public ProjectMemberResponseDto addMember(AddProjectMemberRequestDto requestDto) {
        projectRepository.findById(requestDto.getProjectId())
            .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다. (projectId: " + requestDto.getProjectId() + ")"));

        if (projectMemberRepository.existsByProjectIdAndUserId(requestDto.getProjectId(), requestDto.getUserId())) {
            throw new RuntimeException("이미 프로젝트 멤버입니다. (projectId: " + requestDto.getProjectId() + ", userId: " + requestDto.getUserId() + ")");
        }

        // 멤버 생성 및 저장
        ProjectMember member = ProjectMember.create(requestDto.getProjectId(), requestDto.getUserId());
        ProjectMember savedMember = projectMemberRepository.save(member);

        return toResponseDto(savedMember);
    }

    // 특정 프로젝트의 특정 사용자 멤버 정보 조회
    @Override
    public ProjectMemberResponseDto getMember(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new RuntimeException("프로젝트 멤버를 찾을 수 없습니다. (projectId: " + projectId + ", userId: " + userId + ")"));

        return toResponseDto(member);
    }

    // 프로젝트의 모든 멤버 목록 조회
    @Override
    public List<ProjectMemberResponseDto> getProjectMembers(Long projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);

        return members.stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList());
    }

    // 사용자가 속한 모든 프로젝트 ID 목록 조회
    @Override
    public List<Long> getUserProjects(Long userId) {
        List<ProjectMember> members = projectMemberRepository.findByUserId(userId);

        return members.stream()
            .map(ProjectMember::getProjectId)
            .collect(Collectors.toList());
    }

    // 프로젝트에서 멤버 제거
    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new RuntimeException("프로젝트 멤버를 찾을 수 없습니다. (projectId: " + projectId + ", userId: " + userId + ")"));

        projectMemberRepository.delete(member);
    }

    // 초대 코드를 사용하여 프로젝트에 참여
    @Override
    @Transactional
    public ProjectMemberResponseDto joinByInviteCode(String inviteCode, Long userId) {
        Project project = projectRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다. (inviteCode: " + inviteCode + ")"));

        if (projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId)) {
            throw new RuntimeException("이미 프로젝트 멤버입니다.");
        }

        // 멤버 생성 및 저장
        ProjectMember member = ProjectMember.create(project.getId(), userId);
        ProjectMember savedMember = projectMemberRepository.save(member);

        return toResponseDto(savedMember);
    }

    private ProjectMemberResponseDto toResponseDto(ProjectMember member) {
        return ProjectMemberResponseDto.builder()
            .id(member.getId())
            .projectId(member.getProjectId())
            .userId(member.getUserId())
            .createdAt(member.getCreatedAt())
            .build();
    }
}
