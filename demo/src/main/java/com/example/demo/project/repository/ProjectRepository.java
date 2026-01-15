package com.example.demo.project.repository;

import com.example.demo.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 초대 코드 존재 여부 확인
    boolean existsByInviteCode(String inviteCode);

    // 초대 코드로 프로젝트 조회
    Optional<Project> findByInviteCode(String inviteCode);
}

