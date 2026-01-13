package com.example.demo.project.repository;

import com.example.demo.project.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    // 프로젝트별 멤버 조회
    List<ProjectMember> findByProjectId(Long projectId);
    
    // 사용자별 프로젝트 조회
    List<ProjectMember> findByUserId(Long userId);
    
    // 프로젝트와 사용자로 멤버 조회
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
    
    // 프로젝트에 멤버가 존재하는지 확인
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
