package com.vectasheet.dto;

import com.vectasheet.entity.Spreadsheet;
import java.time.Instant;
import java.util.UUID;

public class SpreadsheetDto {
    private UUID id;
    private UUID workspaceId;
    private String name;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public static SpreadsheetDto from(Spreadsheet s) {
        SpreadsheetDto dto = new SpreadsheetDto();
        dto.id = s.getId();
        dto.workspaceId = s.getWorkspaceId();
        dto.name = s.getName();
        dto.createdBy = s.getCreatedBy();
        dto.createdAt = s.getCreatedAt();
        dto.updatedAt = s.getUpdatedAt();
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public String getName() { return name; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
