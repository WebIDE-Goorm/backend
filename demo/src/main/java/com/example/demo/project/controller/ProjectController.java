package com.example.demo.project.controller;

import com.example.demo.project.dto.request.CreateProjectRequestDto;
import com.example.demo.project.dto.response.ProjectResponseDto;
import com.example.demo.project.service.ProjectService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@RequestBody CreateProjectRequestDto requestDto) {
        ProjectResponseDto response = projectService.createProject(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> getProject(@PathVariable Long projectId) {
        ProjectResponseDto response = projectService.getProject(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {
        List<ProjectResponseDto> response = projectService.getAllProjects();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable Long projectId,
            @RequestBody CreateProjectRequestDto requestDto) {
        ProjectResponseDto response = projectService.updateProject(projectId, requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/invite/{inviteCode}")
    public ResponseEntity<ProjectResponseDto> getProjectByInviteCode(@PathVariable String inviteCode) {
        ProjectResponseDto response = projectService.getProjectByInviteCode(inviteCode);
        return ResponseEntity.ok(response);
    }
}

