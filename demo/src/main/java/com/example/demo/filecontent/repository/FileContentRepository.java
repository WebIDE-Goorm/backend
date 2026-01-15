package com.example.demo.filecontent.repository;

import com.example.demo.filecontent.entity.FileContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileContentRepository extends JpaRepository<FileContent, Long> {

    Optional<FileContent> findFirstByFileIdOrderByVersionDesc(Long fileId);

    List<FileContent> findByFileIdOrderByVersionDesc(Long fileId);

    Optional<FileContent> findByFileIdAndVersion(Long fileId, Integer version);
}

