package com.vectasheet.controller;

import com.vectasheet.dto.*;
import com.vectasheet.entity.ActivityAction;
import com.vectasheet.entity.ActivityEntityType;
import com.vectasheet.entity.NotificationType;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.ActivityLogService;
import com.vectasheet.service.NotificationService;
import com.vectasheet.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public WorkspaceController(
            WorkspaceService workspaceService,
            ActivityLogService activityLogService,
            NotificationService notificationService
    ) {
        this.workspaceService = workspaceService;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateWorkspaceRequest request
    ) {
        return ResponseEntity.ok(workspaceService.create(principal.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(workspaceService.listForUser(principal.getId()));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDto> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId
    ) {
        return ResponseEntity.ok(workspaceService.get(principal.getId(), workspaceId));
    }

    @PatchMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDto> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId,
            @RequestBody UpdateWorkspaceRequest request
    ) {
        return ResponseEntity.ok(workspaceService.update(principal.getId(), workspaceId, request));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId
    ) {
        workspaceService.delete(principal.getId(), workspaceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<List<MemberDto>> listMembers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId
    ) {
        return ResponseEntity.ok(workspaceService.listMembers(principal.getId(), workspaceId));
    }

    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<MemberDto> inviteMember(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody InviteMemberRequest request
    ) {
        MemberDto member = workspaceService.inviteMember(principal.getId(), workspaceId, request);
        activityLogService.log(workspaceId, principal.getId(), ActivityAction.SHARED, ActivityEntityType.MEMBER, member.getUserId(), member.getName());
        notificationService.notify(member.getUserId(), principal.getId(), NotificationType.WORKSPACE_INVITE,
                "You were added to a workspace", workspaceId, workspaceId);
        return ResponseEntity.ok(member);
    }

    @PatchMapping("/{workspaceId}/members/{targetUserId}")
    public ResponseEntity<MemberDto> updateMemberRole(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId,
            @PathVariable UUID targetUserId,
            @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        return ResponseEntity.ok(workspaceService.updateMemberRole(principal.getId(), workspaceId, targetUserId, request));
    }

    @DeleteMapping("/{workspaceId}/members/{targetUserId}")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId,
            @PathVariable UUID targetUserId
    ) {
        workspaceService.removeMember(principal.getId(), workspaceId, targetUserId);
        return ResponseEntity.noContent().build();
    }
}
