package com.vectasheet.dto;

import com.vectasheet.entity.DocumentVersion;
import java.time.Instant;
import java.util.UUID;

public class DocumentVersionDetailDto {
    private UUID id;
    private long versionNumber;
    private String title;
    private String contentHtml;
    private UUID editedBy;
    private Instant createdAt;

    public static DocumentVersionDetailDto from(DocumentVersion v) {
        DocumentVersionDetailDto dto = new DocumentVersionDetailDto();
        dto.id = v.getId();
        dto.versionNumber = v.getVersionNumber();
        dto.title = v.getTitle();
        dto.contentHtml = v.getContentHtml();
        dto.editedBy = v.getEditedBy();
        dto.createdAt = v.getCreatedAt();
        return dto;
    }

    public UUID getId() { return id; }
    public long getVersionNumber() { return versionNumber; }
    public String getTitle() { return title; }
    public String getContentHtml() { return contentHtml; }
    public UUID getEditedBy() { return editedBy; }
    public Instant getCreatedAt() { return createdAt; }
}
