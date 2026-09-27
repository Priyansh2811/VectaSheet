package com.vectasheet.controller;

import com.vectasheet.dto.CommentDto;
import com.vectasheet.dto.CreateCommentRequest;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.DocumentCommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents/{documentId}/comments")
public class DocumentCommentController {

    private final DocumentCommentService commentService;

    public DocumentCommentController(DocumentCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID documentId
    ) {
        return ResponseEntity.ok(commentService.list(principal.getId(), documentId));
    }

    @PostMapping
    public ResponseEntity<CommentDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID documentId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ResponseEntity.ok(commentService.create(principal.getId(), documentId, request));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> resolve(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID documentId,
            @PathVariable UUID commentId,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean resolved = Boolean.TRUE.equals(body.getOrDefault("resolved", true));
        return ResponseEntity.ok(commentService.resolve(principal.getId(), documentId, commentId, resolved));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID documentId,
            @PathVariable UUID commentId
    ) {
        commentService.delete(principal.getId(), documentId, commentId);
        return ResponseEntity.noContent().build();
    }
}
