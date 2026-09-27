package com.vectasheet.controller;

import com.vectasheet.dto.ActivityLogDto;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping("/api/workspaces/{workspaceId}/activity")
    public ResponseEntity<List<ActivityLogDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(activityLogService.list(principal.getId(), workspaceId, limit));
    }
}
