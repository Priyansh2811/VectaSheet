package com.vectasheet.service;

import com.vectasheet.dto.CreateEventRequest;
import com.vectasheet.dto.EventDto;
import com.vectasheet.dto.UpdateEventRequest;
import com.vectasheet.entity.ActivityAction;
import com.vectasheet.entity.ActivityEntityType;
import com.vectasheet.entity.CalendarEvent;
import com.vectasheet.entity.WorkspaceRole;
import com.vectasheet.exception.ApiException;
import com.vectasheet.repository.CalendarEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CalendarEventService {

    private final CalendarEventRepository repository;
    private final WorkspaceService workspaceService;
    private final ActivityLogService activityLogService;

    public CalendarEventService(
            CalendarEventRepository repository,
            WorkspaceService workspaceService,
            ActivityLogService activityLogService
    ) {
        this.repository = repository;
        this.workspaceService = workspaceService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public EventDto create(UUID userId, UUID workspaceId, CreateEventRequest request) {
        workspaceService.requireRoleAtLeast(workspaceId, userId, WorkspaceRole.EDITOR);

        CalendarEvent event = new CalendarEvent();
        event.setWorkspaceId(workspaceId);
        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription());
        event.setStartAt(parseDateTime(request.getStartAt()));
        event.setEndAt(parseDateTime(request.getEndAt()));
        event.setAllDay(request.isAllDay());
        event.setCreatedBy(userId);
        event = repository.save(event);

        activityLogService.log(workspaceId, userId, ActivityAction.CREATED, ActivityEntityType.WORKSPACE, event.getId(), event.getTitle());
        return EventDto.from(event);
    }

    public List<EventDto> list(UUID userId, UUID workspaceId) {
        workspaceService.requireMembership(workspaceId, userId);
        return repository.findByWorkspaceIdOrderByStartAtAsc(workspaceId).stream()
                .filter(e -> !e.isArchived())
                .map(EventDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventDto update(UUID userId, UUID eventId, UpdateEventRequest request) {
        CalendarEvent event = findOrThrow(eventId);
        workspaceService.requireRoleAtLeast(event.getWorkspaceId(), userId, WorkspaceRole.EDITOR);

        if (request.getTitle() != null && !request.getTitle().isBlank()) event.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getStartAt() != null) event.setStartAt(parseDateTime(request.getStartAt()));
        if (request.getEndAt() != null) event.setEndAt(parseDateTime(request.getEndAt()));
        if (request.getAllDay() != null) event.setAllDay(request.getAllDay());

        event = repository.save(event);
        return EventDto.from(event);
    }

    @Transactional
    public void delete(UUID userId, UUID eventId) {
        CalendarEvent event = findOrThrow(eventId);
        workspaceService.requireRoleAtLeast(event.getWorkspaceId(), userId, WorkspaceRole.EDITOR);
        event.setArchived(true);
        repository.save(event);
    }

    private LocalDateTime parseDateTime(String s) {
        try {
            return LocalDateTime.parse(s);
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid date/time (expected ISO format): " + s);
        }
    }

    private CalendarEvent findOrThrow(UUID eventId) {
        CalendarEvent event = repository.findById(eventId)
                .orElseThrow(() -> ApiException.notFound("Event not found"));
        if (event.isArchived()) throw ApiException.notFound("Event not found");
        return event;
    }
}
