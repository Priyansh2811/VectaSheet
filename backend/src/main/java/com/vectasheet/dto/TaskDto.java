package com.vectasheet.dto;

import com.vectasheet.entity.Task;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TaskDto {
    private UUID id;
    private UUID workspaceId;
    private UUID parentTaskId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private UUID assigneeId;
    private UUID createdBy;
    private LocalDate startDate;
    private LocalDate dueDate;
    private int position;
    private Instant createdAt;
    private Instant updatedAt;
    private int subtaskCount;
    private int subtaskDoneCount;

    public static TaskDto from(Task t) {
        TaskDto dto = new TaskDto();
        dto.id = t.getId();
        dto.workspaceId = t.getWorkspaceId();
        dto.parentTaskId = t.getParentTaskId();
        dto.title = t.getTitle();
        dto.description = t.getDescription();
        dto.status = t.getStatus().name();
        dto.priority = t.getPriority().name();
        dto.assigneeId = t.getAssigneeId();
        dto.createdBy = t.getCreatedBy();
        dto.startDate = t.getStartDate();
        dto.dueDate = t.getDueDate();
        dto.position = t.getPosition();
        dto.createdAt = t.getCreatedAt();
        dto.updatedAt = t.getUpdatedAt();
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public UUID getParentTaskId() { return parentTaskId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public UUID getAssigneeId() { return assigneeId; }
    public UUID getCreatedBy() { return createdBy; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public int getPosition() { return position; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public int getSubtaskCount() { return subtaskCount; }
    public void setSubtaskCount(int subtaskCount) { this.subtaskCount = subtaskCount; }
    public int getSubtaskDoneCount() { return subtaskDoneCount; }
    public void setSubtaskDoneCount(int subtaskDoneCount) { this.subtaskDoneCount = subtaskDoneCount; }
}
