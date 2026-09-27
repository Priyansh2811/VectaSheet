package com.vectasheet.dto;

public class UpdateTaskRequest {
    private String title;
    private String description;
    private String status;
    private String priority;
    private String assigneeId;
    private String startDate;
    private String dueDate;
    /** Optional: new position within its status column, for Kanban drag-and-drop. */
    private Integer position;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getAssigneeId() { return assigneeId; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
