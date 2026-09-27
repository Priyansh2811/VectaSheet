package com.vectasheet.dto;

import com.vectasheet.entity.DocumentVersion;
import java.time.Instant;
import java.util.UUID;

public class DocumentVersionDto {
    private UUID id;
    private long versionNumber;
    private String title;
    private UUID editedBy;
    private Instant createdAt;
    private Long restoredFromVersion;

    public static DocumentVersionDto summary(DocumentVersion v) {
        DocumentVersionDto dto = new DocumentVersionDto();
        dto.id = v.getId();
        dto.versionNumber = v.getVersionNumber();
        dto.title = v.getTitle();
        dto.editedBy = v.getEditedBy();
        dto.createdAt = v.getCreatedAt();
        dto.restoredFromVersion = v.getRestoredFromVersion();
        return dto;
    }

    public UUID getId() { return id; }
    public long getVersionNumber() { return versionNumber; }
    public String getTitle() { return title; }
    public UUID getEditedBy() { return editedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getRestoredFromVersion() { return restoredFromVersion; }
}
