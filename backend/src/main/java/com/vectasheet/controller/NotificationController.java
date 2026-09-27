package com.vectasheet.controller;

import com.vectasheet.dto.NotificationDto;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.list(principal.getId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(principal.getId())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NotificationDto> setRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean read = Boolean.TRUE.equals(body.getOrDefault("read", true));
        return ResponseEntity.ok(notificationService.setRead(principal.getId(), id, read));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllRead(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        notificationService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
