package com.vectasheet.repository;

import com.vectasheet.entity.DocumentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DocumentCommentRepository extends JpaRepository<DocumentComment, UUID> {
    List<DocumentComment> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);
}
