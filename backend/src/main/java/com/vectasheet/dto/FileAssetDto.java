package com.vectasheet.dto;

import com.vectasheet.entity.FileAsset;
import java.time.Instant;
import java.util.UUID;

public class FileAssetDto {
    private UUID id;
    private UUID workspaceId;
    private String originalFilename;
    private String contentType;
    private long sizeBytes;
    private UUID uploadedBy;
    private Instant createdAt;

    public static FileAssetDto from(FileAsset f) {
        FileAssetDto dto = new FileAssetDto();
        dto.id = f.getId();
        dto.workspaceId = f.getWorkspaceId();
        dto.originalFilename = f.getOriginalFilename();
        dto.contentType = f.getContentType();
        dto.sizeBytes = f.getSizeBytes();
        dto.uploadedBy = f.getUploadedBy();
        dto.createdAt = f.getCreatedAt();
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public String getOriginalFilename() { return originalFilename; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public UUID getUploadedBy() { return uploadedBy; }
    public Instant getCreatedAt() { return createdAt; }
}
