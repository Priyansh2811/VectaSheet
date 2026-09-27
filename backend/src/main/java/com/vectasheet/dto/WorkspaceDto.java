package com.vectasheet.dto;

import com.vectasheet.entity.Workspace;
import com.vectasheet.entity.WorkspaceRole;
import java.time.Instant;
import java.util.UUID;

public class WorkspaceDto {
    private UUID id;
    private String name;
    private String description;
    private UUID ownerId;
    private WorkspaceRole myRole;
    private Instant createdAt;
    private Instant updatedAt;
    private int memberCount;

    public static WorkspaceDto from(Workspace w, WorkspaceRole myRole, int memberCount) {
        WorkspaceDto dto = new WorkspaceDto();
        dto.id = w.getId();
        dto.name = w.getName();
        dto.description = w.getDescription();
        dto.ownerId = w.getOwnerId();
        dto.myRole = myRole;
        dto.createdAt = w.getCreatedAt();
        dto.updatedAt = w.getUpdatedAt();
        dto.memberCount = memberCount;
        return dto;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public UUID getOwnerId() { return ownerId; }
    public WorkspaceRole getMyRole() { return myRole; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public int getMemberCount() { return memberCount; }
}
