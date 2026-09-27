package com.vectasheet.dto;

import com.vectasheet.entity.DocumentComment;
import java.time.Instant;
import java.util.UUID;

public class CommentDto {
    private UUID id;
    private UUID authorId;
    private String authorName;
    private String body;
    private String quotedText;
    private boolean resolved;
    private Instant createdAt;

    public static CommentDto from(DocumentComment c, String authorName) {
        CommentDto dto = new CommentDto();
        dto.id = c.getId();
        dto.authorId = c.getAuthorId();
        dto.authorName = authorName;
        dto.body = c.getBody();
        dto.quotedText = c.getQuotedText();
        dto.resolved = c.isResolved();
        dto.createdAt = c.getCreatedAt();
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getBody() { return body; }
    public String getQuotedText() { return quotedText; }
    public boolean isResolved() { return resolved; }
    public Instant getCreatedAt() { return createdAt; }
}
