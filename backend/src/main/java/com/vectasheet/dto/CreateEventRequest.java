package com.vectasheet.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateEventRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    @NotBlank(message = "Start time is required")
    private String startAt;
    @NotBlank(message = "End time is required")
    private String endAt;
    private boolean allDay = false;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStartAt() { return startAt; }
    public void setStartAt(String startAt) { this.startAt = startAt; }
    public String getEndAt() { return endAt; }
    public void setEndAt(String endAt) { this.endAt = endAt; }
    public boolean isAllDay() { return allDay; }
    public void setAllDay(boolean allDay) { this.allDay = allDay; }
}
