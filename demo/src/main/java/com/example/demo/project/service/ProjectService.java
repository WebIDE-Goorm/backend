package com.example.demo.project.service;

import com.example.demo.project.dto.request.CreateProjectRequestDto;
import com.example.demo.project.dto.response.ProjectResponseDto;

import java.util.List;

public interface ProjectService {
    //프로젝트 생성
    ProjectResponseDto createProject(CreateProjectRequestDto requestDto, Long userId);

    //프로젝트 조회
    ProjectResponseDto getProject(Long projectId, Long userId);

    //프로젝트 목록 조회
    List<ProjectResponseDto> getAllProjects();

    //프로젝트 수정
    ProjectResponseDto updateProject(Long projectId, CreateProjectRequestDto requestDto, Long userId);

    //프로젝트 삭제
    void deleteProject(Long projectId, Long userId);

    //초대 코드로 프로젝트 조회
    ProjectResponseDto getProjectByInviteCode(String inviteCode);

    //사용자가 속한 프로젝트 목록 조회
    List<ProjectResponseDto> getProjectsByUserId(Long userId);
}

