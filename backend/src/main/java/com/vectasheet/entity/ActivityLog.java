package com.vectasheet.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID workspaceId;

    @Column(nullable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityEntityType entityType;

    private UUID entityId;

    /** Short human-readable label, e.g. the task title or document name, captured at log time. */
    private String entityLabel;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }
    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }
    public ActivityAction getAction() { return action; }
    public void setAction(ActivityAction action) { this.action = action; }
    public ActivityEntityType getEntityType() { return entityType; }
    public void setEntityType(ActivityEntityType entityType) { this.entityType = entityType; }
    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public String getEntityLabel() { return entityLabel; }
    public void setEntityLabel(String entityLabel) { this.entityLabel = entityLabel; }
    public Instant getCreatedAt() { return createdAt; }
}
