package com.vectasheet.repository;

import com.vectasheet.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {
    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(UUID documentId);
    long countByDocumentId(UUID documentId);
}
