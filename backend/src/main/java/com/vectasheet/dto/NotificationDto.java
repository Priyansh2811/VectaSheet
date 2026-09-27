package com.vectasheet.dto;

import com.vectasheet.entity.Notification;
import java.time.Instant;
import java.util.UUID;

public class NotificationDto {
    private UUID id;
    private UUID actorId;
    private String type;
    private String message;
    private UUID workspaceId;
    private UUID entityId;
    private boolean read;
    private Instant createdAt;

    public static NotificationDto from(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.id = n.getId();
        dto.actorId = n.getActorId();
        dto.type = n.getType().name();
        dto.message = n.getMessage();
        dto.workspaceId = n.getWorkspaceId();
        dto.entityId = n.getEntityId();
        dto.read = n.isRead();
        dto.createdAt = n.getCreatedAt();
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getActorId() { return actorId; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public UUID getWorkspaceId() { return workspaceId; }
    public UUID getEntityId() { return entityId; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}
