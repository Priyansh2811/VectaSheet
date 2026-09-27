package com.vectasheet.dto;

import com.vectasheet.entity.Document;
import java.time.Instant;
import java.util.UUID;

public class DocumentDto {
    private UUID id;
    private UUID workspaceId;
    private String title;
    private String contentHtml;
    private UUID createdBy;
    private UUID lastEditedBy;
    private long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static DocumentDto from(Document d) {
        DocumentDto dto = new DocumentDto();
        dto.id = d.getId();
        dto.workspaceId = d.getWorkspaceId();
        dto.title = d.getTitle();
        dto.contentHtml = d.getContentHtml();
        dto.createdBy = d.getCreatedBy();
        dto.lastEditedBy = d.getLastEditedBy();
        dto.version = d.getVersion();
        dto.createdAt = d.getCreatedAt();
        dto.updatedAt = d.getUpdatedAt();
        return dto;
    }

    public static DocumentDto summary(Document d) {
        DocumentDto dto = from(d);
        dto.contentHtml = null; // list views don't need full body
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public String getTitle() { return title; }
    public String getContentHtml() { return contentHtml; }
    public UUID getCreatedBy() { return createdBy; }
    public UUID getLastEditedBy() { return lastEditedBy; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
