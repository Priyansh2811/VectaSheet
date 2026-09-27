package com.vectasheet.controller;

import com.vectasheet.dto.CreateTaskRequest;
import com.vectasheet.dto.TaskDto;
import com.vectasheet.dto.UpdateTaskRequest;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/api/workspaces/{workspaceId}/tasks")
    public ResponseEntity<TaskDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.create(principal.getId(), workspaceId, request));
    }

    @GetMapping("/api/workspaces/{workspaceId}/tasks")
    public ResponseEntity<List<TaskDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId
    ) {
        return ResponseEntity.ok(taskService.list(principal.getId(), workspaceId));
    }

    @GetMapping("/api/tasks/{taskId}")
    public ResponseEntity<TaskDto> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID taskId
    ) {
        return ResponseEntity.ok(taskService.get(principal.getId(), taskId));
    }

    @PatchMapping("/api/tasks/{taskId}")
    public ResponseEntity<TaskDto> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID taskId,
            @RequestBody UpdateTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.update(principal.getId(), taskId, request));
    }

    @DeleteMapping("/api/tasks/{taskId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID taskId
    ) {
        taskService.delete(principal.getId(), taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/tasks/{taskId}/subtasks")
    public ResponseEntity<List<TaskDto>> listSubtasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID taskId
    ) {
        return ResponseEntity.ok(taskService.listSubtasks(principal.getId(), taskId));
    }
}
