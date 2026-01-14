package com.example.demo.project.controller;

import com.example.demo.project.dto.request.AddProjectMemberRequestDto;
import com.example.demo.project.dto.response.ProjectMemberResponseDto;
import com.example.demo.project.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping
    public ResponseEntity<ProjectMemberResponseDto> addMember(
            @PathVariable Long projectId,
            @RequestBody AddProjectMemberRequestDto requestDto) {
        requestDto.setProjectId(projectId);
        ProjectMemberResponseDto response = projectMemberService.addMember(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectMemberResponseDto>> getProjectMembers(@PathVariable Long projectId) {
        List<ProjectMemberResponseDto> response = projectMemberService.getProjectMembers(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProjectMemberResponseDto> getMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        ProjectMemberResponseDto response = projectMemberService.getMember(projectId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        projectMemberService.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/join")
    public ResponseEntity<ProjectMemberResponseDto> joinByInviteCode(
            @RequestParam String inviteCode,
            @RequestParam Long userId) {
        ProjectMemberResponseDto response = projectMemberService.joinByInviteCode(inviteCode, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
