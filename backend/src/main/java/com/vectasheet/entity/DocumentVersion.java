package com.vectasheet.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * An immutable snapshot of a document at a point in time. Restoring a version
 * never deletes history — it copies the old snapshot's content into the live
 * document and creates a brand new version entry, per the "rollback is itself
 * a new version" rule.
 */
@Entity
@Table(name = "document_versions")
public class DocumentVersion {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private long versionNumber;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String contentHtml;

    @Column(nullable = false)
    private UUID editedBy;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    /** Null for a normal edit snapshot; set to the source version number when this snapshot is a restore. */
    private Long restoredFromVersion;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }
    public long getVersionNumber() { return versionNumber; }
    public void setVersionNumber(long versionNumber) { this.versionNumber = versionNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }
    public UUID getEditedBy() { return editedBy; }
    public void setEditedBy(UUID editedBy) { this.editedBy = editedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getRestoredFromVersion() { return restoredFromVersion; }
    public void setRestoredFromVersion(Long restoredFromVersion) { this.restoredFromVersion = restoredFromVersion; }
}
