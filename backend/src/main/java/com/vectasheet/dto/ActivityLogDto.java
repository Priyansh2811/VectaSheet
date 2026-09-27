package com.vectasheet.dto;

import com.vectasheet.entity.ActivityLog;
import java.time.Instant;
import java.util.UUID;

public class ActivityLogDto {
    private UUID id;
    private UUID actorId;
    private String action;
    private String entityType;
    private UUID entityId;
    private String entityLabel;
    private Instant createdAt;

    public static ActivityLogDto from(ActivityLog a) {
        ActivityLogDto dto = new ActivityLogDto();
        dto.id = a.getId();
        dto.actorId = a.getActorId();
        dto.action = a.getAction().name();
        dto.entityType = a.getEntityType().name();
        dto.entityId = a.getEntityId();
        dto.entityLabel = a.getEntityLabel();
        dto.createdAt = a.getCreatedAt();
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getActorId() { return actorId; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public String getEntityLabel() { return entityLabel; }
    public Instant getCreatedAt() { return createdAt; }
}
