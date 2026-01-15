package com.example.demo.file.repository;

import com.example.demo.file.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    // 프로젝트별 삭제되지 않은 파일 조회
    List<FileEntity> findByProjectIdAndIsDeletedFalse(Long projectId);

    // 프로젝트의 루트 파일 조회
    List<FileEntity> findByProjectIdAndParentIdIsNullAndIsDeletedFalse(Long projectId);

    // 특정 부모의 자식 파일 조회
    List<FileEntity> findByParentIdAndIsDeletedFalse(Long parentId);

    // 파일 존재 여부 조회
    boolean existsByProjectIdAndParentIdAndNameAndIsDeletedFalse(Long projectId, Long parentId, String name);

    // 파일 존재 여부 조회 (특정 ID 제외)
    boolean existsByProjectIdAndParentIdAndNameAndIdNotAndIsDeletedFalse(Long projectId, Long parentId, String name, Long excludeId);

    // 파일 조회 (삭제되지 않은 것만)
    Optional<FileEntity> findByIdAndIsDeletedFalse(Long id);
}

