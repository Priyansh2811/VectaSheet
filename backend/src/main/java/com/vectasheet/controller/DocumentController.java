package com.vectasheet.controller;

import com.vectasheet.dto.*;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/api/workspaces/{workspaceId}/documents")
    public ResponseEntity<DocumentDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId,
            @RequestBody CreateDocumentRequest request
    ) {
        return ResponseEntity.ok(documentService.create(principal.getId(), workspaceId, request));
    }

    @GetMapping("/api/workspaces/{workspaceId}/documents")
    public ResponseEntity<List<DocumentDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId
    ) {
        return ResponseEntity.ok(documentService.list(principal.getId(), workspaceId));
    }

    @GetMapping("/api/documents/{documentId}")
    public ResponseEntity<DocumentDto> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID documentId
    ) {
        return ResponseEntity.ok(documentService.get(principal.getId(), documentId));
    }

    @PutMapping("/api/documents/{documentId}")
    public ResponseEntity<DocumentDto> save(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID documentId,
            @RequestBody UpdateDocumentRequest request
    ) {
        return ResponseEntity.ok(documentService.save(principal.getId(), documentId, request));
    }

    @DeleteMapping("/api/documents/{documentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID documentId
    ) {
        documentService.delete(principal.getId(), documentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/documents/{documentId}/versions")
    public ResponseEntity<List<DocumentVersionDto>> listVersions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID documentId
    ) {
        return ResponseEntity.ok(documentService.listVersions(principal.getId(), documentId));
    }

    @GetMapping("/api/documents/{documentId}/versions/{versionId}")
    public ResponseEntity<DocumentVersionDetailDto> getVersion(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID documentId,
            @PathVariable UUID versionId
    ) {
        return ResponseEntity.ok(documentService.getVersion(principal.getId(), documentId, versionId));
    }

    @PostMapping("/api/documents/{documentId}/versions/{versionId}/restore")
    public ResponseEntity<DocumentDto> restoreVersion(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID documentId,
            @PathVariable UUID versionId
    ) {
        return ResponseEntity.ok(documentService.restoreVersion(principal.getId(), documentId, versionId));
    }
}
