package com.vectasheet.service;

import com.vectasheet.dto.CommentDto;
import com.vectasheet.dto.CreateCommentRequest;
import com.vectasheet.entity.ActivityAction;
import com.vectasheet.entity.ActivityEntityType;
import com.vectasheet.entity.Document;
import com.vectasheet.entity.DocumentComment;
import com.vectasheet.entity.NotificationType;
import com.vectasheet.entity.User;
import com.vectasheet.entity.WorkspaceRole;
import com.vectasheet.exception.ApiException;
import com.vectasheet.repository.DocumentCommentRepository;
import com.vectasheet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentCommentService {

    private final DocumentCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final DocumentService documentService;
    private final WorkspaceService workspaceService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public DocumentCommentService(
            DocumentCommentRepository commentRepository,
            UserRepository userRepository,
            DocumentService documentService,
            WorkspaceService workspaceService,
            ActivityLogService activityLogService,
            NotificationService notificationService
    ) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.documentService = documentService;
        this.workspaceService = workspaceService;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    public List<CommentDto> list(UUID userId, UUID documentId) {
        Document doc = documentService.findOrThrow(documentId);
        workspaceService.requireMembership(doc.getWorkspaceId(), userId);

        return commentRepository.findByDocumentIdOrderByCreatedAtAsc(documentId).stream()
                .map(c -> CommentDto.from(c, authorName(c.getAuthorId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto create(UUID userId, UUID documentId, CreateCommentRequest request) {
        Document doc = documentService.findOrThrow(documentId);
        workspaceService.requireRoleAtLeast(doc.getWorkspaceId(), userId, WorkspaceRole.COMMENTER);

        DocumentComment comment = new DocumentComment();
        comment.setDocumentId(documentId);
        comment.setAuthorId(userId);
        comment.setBody(request.getBody().trim());
        comment.setQuotedText(request.getQuotedText());
        comment = commentRepository.save(comment);

        activityLogService.log(doc.getWorkspaceId(), userId, ActivityAction.COMMENTED, ActivityEntityType.DOCUMENT, documentId, doc.getTitle());
        notifyMentions(doc, userId, comment.getBody());

        return CommentDto.from(comment, authorName(userId));
    }

    @Transactional
    public CommentDto resolve(UUID userId, UUID documentId, UUID commentId, boolean resolved) {
        Document doc = documentService.findOrThrow(documentId);
        workspaceService.requireRoleAtLeast(doc.getWorkspaceId(), userId, WorkspaceRole.COMMENTER);

        DocumentComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ApiException.notFound("Comment not found"));
        if (!comment.getDocumentId().equals(documentId)) {
            throw ApiException.notFound("Comment not found");
        }

        comment.setResolved(resolved);
        comment = commentRepository.save(comment);
        return CommentDto.from(comment, authorName(comment.getAuthorId()));
    }

    @Transactional
    public void delete(UUID userId, UUID documentId, UUID commentId) {
        Document doc = documentService.findOrThrow(documentId);
        DocumentComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ApiException.notFound("Comment not found"));
        if (!comment.getDocumentId().equals(documentId)) {
            throw ApiException.notFound("Comment not found");
        }

        boolean isAuthor = comment.getAuthorId().equals(userId);
        if (!isAuthor) {
            workspaceService.requireRoleAtLeast(doc.getWorkspaceId(), userId, WorkspaceRole.ADMIN);
        } else {
            workspaceService.requireMembership(doc.getWorkspaceId(), userId);
        }

        commentRepository.delete(comment);
    }

    private String authorName(UUID userId) {
        return userRepository.findById(userId).map(User::getName).orElse("Unknown");
    }

    /**
     * Simple @mention detection: for every workspace member whose name (or its
     * first word) appears as "@Name" in the comment, send a mention notification.
     * This is a plain substring match, not a rich-text @-picker with stored user
     * IDs — good enough to prove the notification path works end-to-end, but a
     * real mention feature would resolve mentions at compose time instead.
     */
    private void notifyMentions(Document doc, UUID authorId, String body) {
        String lowerBody = body.toLowerCase();
        for (var member : workspaceService.listMembers(authorId, doc.getWorkspaceId())) {
            if (member.getUserId().equals(authorId)) continue;
            String firstName = member.getName().split("\\s+")[0].toLowerCase();
            if (lowerBody.contains("@" + firstName)) {
                notificationService.notify(member.getUserId(), authorId, NotificationType.MENTION,
                        authorName(authorId) + " mentioned you in \"" + doc.getTitle() + "\"", doc.getWorkspaceId(), doc.getId());
            }
        }
    }
}
