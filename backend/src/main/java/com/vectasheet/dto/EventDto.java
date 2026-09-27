package com.vectasheet.dto;

import com.vectasheet.entity.CalendarEvent;
import java.time.LocalDateTime;
import java.util.UUID;

public class EventDto {
    private UUID id;
    private UUID workspaceId;
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean allDay;
    private UUID createdBy;

    public static EventDto from(CalendarEvent e) {
        EventDto dto = new EventDto();
        dto.id = e.getId();
        dto.workspaceId = e.getWorkspaceId();
        dto.title = e.getTitle();
        dto.description = e.getDescription();
        dto.startAt = e.getStartAt();
        dto.endAt = e.getEndAt();
        dto.allDay = e.isAllDay();
        dto.createdBy = e.getCreatedBy();
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public boolean isAllDay() { return allDay; }
    public UUID getCreatedBy() { return createdBy; }
}
