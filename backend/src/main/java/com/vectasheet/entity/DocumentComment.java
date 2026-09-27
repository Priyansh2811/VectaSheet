package com.vectasheet.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_comments")
public class DocumentComment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private UUID authorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /** Optional short quoted excerpt the comment refers to, shown for context (not live-anchored yet). */
    private String quotedText;

    private boolean resolved = false;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getQuotedText() { return quotedText; }
    public void setQuotedText(String quotedText) { this.quotedText = quotedText; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    public Instant getCreatedAt() { return createdAt; }
}
