package com.vectasheet.controller;

import com.vectasheet.dto.CreateEventRequest;
import com.vectasheet.dto.EventDto;
import com.vectasheet.dto.UpdateEventRequest;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.CalendarEventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CalendarEventController {

    private final CalendarEventService eventService;

    public CalendarEventController(CalendarEventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/api/workspaces/{workspaceId}/events")
    public ResponseEntity<EventDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateEventRequest request
    ) {
        return ResponseEntity.ok(eventService.create(principal.getId(), workspaceId, request));
    }

    @GetMapping("/api/workspaces/{workspaceId}/events")
    public ResponseEntity<List<EventDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId
    ) {
        return ResponseEntity.ok(eventService.list(principal.getId(), workspaceId));
    }

    @PatchMapping("/api/events/{eventId}")
    public ResponseEntity<EventDto> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID eventId,
            @RequestBody UpdateEventRequest request
    ) {
        return ResponseEntity.ok(eventService.update(principal.getId(), eventId, request));
    }

    @DeleteMapping("/api/events/{eventId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID eventId
    ) {
        eventService.delete(principal.getId(), eventId);
        return ResponseEntity.noContent().build();
    }
}
