package com.vectasheet.service;

import com.vectasheet.dto.*;
import com.vectasheet.entity.ActivityAction;
import com.vectasheet.entity.ActivityEntityType;
import com.vectasheet.entity.Document;
import com.vectasheet.entity.DocumentVersion;
import com.vectasheet.entity.WorkspaceRole;
import com.vectasheet.exception.ApiException;
import com.vectasheet.repository.DocumentRepository;
import com.vectasheet.repository.DocumentVersionRepository;
import com.vectasheet.util.HtmlSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final WorkspaceService workspaceService;
    private final ActivityLogService activityLogService;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentVersionRepository versionRepository,
            WorkspaceService workspaceService,
            ActivityLogService activityLogService
    ) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.workspaceService = workspaceService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public DocumentDto create(UUID userId, UUID workspaceId, CreateDocumentRequest request) {
        workspaceService.requireRoleAtLeast(workspaceId, userId, WorkspaceRole.EDITOR);

        Document doc = new Document();
        doc.setWorkspaceId(workspaceId);
        doc.setTitle(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle().trim() : "Untitled document");
        doc.setContentHtml("");
        doc.setCreatedBy(userId);
        doc.setLastEditedBy(userId);
        doc = documentRepository.save(doc);

        snapshot(doc, userId, null);
        activityLogService.log(workspaceId, userId, ActivityAction.CREATED, ActivityEntityType.DOCUMENT, doc.getId(), doc.getTitle());

        return DocumentDto.from(doc);
    }

    public List<DocumentDto> list(UUID userId, UUID workspaceId) {
        workspaceService.requireMembership(workspaceId, userId);
        return documentRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId).stream()
                .filter(d -> !d.isArchived())
                .map(DocumentDto::summary)
                .collect(Collectors.toList());
    }

    public DocumentDto get(UUID userId, UUID documentId) {
        Document doc = findOrThrow(documentId);
        workspaceService.requireMembership(doc.getWorkspaceId(), userId);
        return DocumentDto.from(doc);
    }

    /**
     * Autosave endpoint: the frontend debounces keystrokes and calls this roughly
     * every ~1.5s of inactivity. Every call creates a new version snapshot, so
     * history is fine-grained; a heavier app might coalesce snapshots, but this
     * keeps the rollback story simple and honest.
     */
    @Transactional
    public DocumentDto save(UUID userId, UUID documentId, UpdateDocumentRequest request) {
        Document doc = findOrThrow(documentId);
        workspaceService.requireRoleAtLeast(doc.getWorkspaceId(), userId, WorkspaceRole.EDITOR);

        if (request.getExpectedVersion() >= 0 && doc.getVersion() != request.getExpectedVersion()) {
            throw ApiException.conflict(
                    "This document was changed by another user since you last loaded it. "
                    + "Your version: " + request.getExpectedVersion() + ", current version: " + doc.getVersion());
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            doc.setTitle(request.getTitle().trim());
        }
        if (request.getContentHtml() != null) {
            doc.setContentHtml(HtmlSanitizer.sanitize(request.getContentHtml()));
        }
        doc.setVersion(doc.getVersion() + 1);
        doc.setLastEditedBy(userId);
        doc = documentRepository.save(doc);

        snapshot(doc, userId, null);
        activityLogService.log(doc.getWorkspaceId(), userId, ActivityAction.EDITED, ActivityEntityType.DOCUMENT, doc.getId(), doc.getTitle());

        return DocumentDto.from(doc);
    }

    @Transactional
    public void delete(UUID userId, UUID documentId) {
        Document doc = findOrThrow(documentId);
        workspaceService.requireRoleAtLeast(doc.getWorkspaceId(), userId, WorkspaceRole.ADMIN);
        doc.setArchived(true);
        documentRepository.save(doc);
        activityLogService.log(doc.getWorkspaceId(), userId, ActivityAction.DELETED, ActivityEntityType.DOCUMENT, doc.getId(), doc.getTitle());
    }

    public List<DocumentVersionDto> listVersions(UUID userId, UUID documentId) {
        Document doc = findOrThrow(documentId);
        workspaceService.requireMembership(doc.getWorkspaceId(), userId);
        return versionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId).stream()
                .map(DocumentVersionDto::summary)
                .collect(Collectors.toList());
    }

    public DocumentVersionDetailDto getVersion(UUID userId, UUID documentId, UUID versionId) {
        Document doc = findOrThrow(documentId);
        workspaceService.requireMembership(doc.getWorkspaceId(), userId);
        DocumentVersion v = versionRepository.findById(versionId)
                .orElseThrow(() -> ApiException.notFound("Version not found"));
        if (!v.getDocumentId().equals(documentId)) {
            throw ApiException.notFound("Version not found");
        }
        return DocumentVersionDetailDto.from(v);
    }

    /** Restoring never deletes history: it copies the old snapshot into the live doc and logs a new version. */
    @Transactional
    public DocumentDto restoreVersion(UUID userId, UUID documentId, UUID versionId) {
        Document doc = findOrThrow(documentId);
        workspaceService.requireRoleAtLeast(doc.getWorkspaceId(), userId, WorkspaceRole.EDITOR);

        DocumentVersion source = versionRepository.findById(versionId)
                .orElseThrow(() -> ApiException.notFound("Version not found"));
        if (!source.getDocumentId().equals(documentId)) {
            throw ApiException.notFound("Version not found");
        }

        doc.setTitle(source.getTitle());
        doc.setContentHtml(source.getContentHtml());
        doc.setVersion(doc.getVersion() + 1);
        doc.setLastEditedBy(userId);
        doc = documentRepository.save(doc);

        snapshot(doc, userId, source.getVersionNumber());
        activityLogService.log(doc.getWorkspaceId(), userId, ActivityAction.RESTORED, ActivityEntityType.DOCUMENT, doc.getId(), doc.getTitle());

        return DocumentDto.from(doc);
    }

    private void snapshot(Document doc, UUID userId, Long restoredFromVersion) {
        DocumentVersion v = new DocumentVersion();
        v.setDocumentId(doc.getId());
        v.setVersionNumber(doc.getVersion());
        v.setTitle(doc.getTitle());
        v.setContentHtml(doc.getContentHtml());
        v.setEditedBy(userId);
        v.setRestoredFromVersion(restoredFromVersion);
        versionRepository.save(v);
    }

    public Document findOrThrow(UUID documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> ApiException.notFound("Document not found"));
        if (doc.isArchived()) {
            throw ApiException.notFound("Document not found");
        }
        return doc;
    }
}
