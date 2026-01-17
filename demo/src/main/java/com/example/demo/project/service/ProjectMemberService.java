package com.example.demo.project.service;

import com.example.demo.project.dto.request.AddProjectMemberRequestDto;
import com.example.demo.project.dto.response.ProjectMemberResponseDto;

import java.util.List;

public interface ProjectMemberService {
    // 프로젝트 멤버 추가
    ProjectMemberResponseDto addMember(AddProjectMemberRequestDto requestDto);
    
    // 프로젝트 멤버 조회
    ProjectMemberResponseDto getMember(Long projectId, Long userId);
    
    // 프로젝트의 모든 멤버 조회
    List<ProjectMemberResponseDto> getProjectMembers(Long projectId);
    
    // 사용자가 속한 프로젝트 ID 목록 조회
    List<Long> getUserProjects(Long userId);
    
    // 프로젝트 멤버 삭제
    void removeMember(Long projectId, Long userId);
    
    // 초대 코드로 프로젝트 참여
    ProjectMemberResponseDto joinByInviteCode(String inviteCode, Long userId);
    
    // 프로젝트 멤버 여부 검증
    void validateProjectMember(Long projectId, Long userId);
}
