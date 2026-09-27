package com.vectasheet.service;

import com.vectasheet.dto.CreateTaskRequest;
import com.vectasheet.dto.TaskDto;
import com.vectasheet.dto.UpdateTaskRequest;
import com.vectasheet.entity.ActivityAction;
import com.vectasheet.entity.ActivityEntityType;
import com.vectasheet.entity.Task;
import com.vectasheet.entity.TaskPriority;
import com.vectasheet.entity.TaskStatus;
import com.vectasheet.entity.WorkspaceRole;
import com.vectasheet.entity.NotificationType;
import com.vectasheet.exception.ApiException;
import com.vectasheet.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final WorkspaceService workspaceService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public TaskService(
            TaskRepository taskRepository,
            WorkspaceService workspaceService,
            ActivityLogService activityLogService,
            NotificationService notificationService
    ) {
        this.taskRepository = taskRepository;
        this.workspaceService = workspaceService;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    @Transactional
    public TaskDto create(UUID userId, UUID workspaceId, CreateTaskRequest request) {
        workspaceService.requireRoleAtLeast(workspaceId, userId, WorkspaceRole.EDITOR);

        Task task = new Task();
        task.setWorkspaceId(workspaceId);
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setCreatedBy(userId);
        task.setPriority(parsePriority(request.getPriority()));
        task.setAssigneeId(parseUuidOrNull(request.getAssigneeId()));
        task.setStartDate(parseDateOrNull(request.getStartDate()));
        task.setDueDate(parseDateOrNull(request.getDueDate()));

        UUID parentId = parseUuidOrNull(request.getParentTaskId());
        if (parentId != null) {
            Task parent = taskRepository.findById(parentId)
                    .orElseThrow(() -> ApiException.notFound("Parent task not found"));
            if (!parent.getWorkspaceId().equals(workspaceId)) {
                throw ApiException.badRequest("Parent task belongs to a different workspace");
            }
            task.setParentTaskId(parentId);
            long siblingCount = taskRepository.findByParentTaskIdOrderByPositionAsc(parentId).size();
            task.setPosition((int) siblingCount);
        } else {
            long columnCount = taskRepository.findByWorkspaceIdAndParentTaskIdIsNullOrderByPositionAsc(workspaceId)
                    .stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
            task.setPosition((int) columnCount);
        }

        task = taskRepository.save(task);
        activityLogService.log(workspaceId, userId, ActivityAction.CREATED, ActivityEntityType.TASK, task.getId(), task.getTitle());
        if (task.getAssigneeId() != null) {
            notificationService.notify(task.getAssigneeId(), userId, NotificationType.TASK_ASSIGNED,
                    "You were assigned to \"" + task.getTitle() + "\"", workspaceId, task.getId());
        }
        return toDtoWithSubtaskCounts(task);
    }

    public List<TaskDto> list(UUID userId, UUID workspaceId) {
        workspaceService.requireMembership(workspaceId, userId);
        return taskRepository.findByWorkspaceIdAndParentTaskIdIsNullOrderByPositionAsc(workspaceId).stream()
                .filter(t -> !t.isArchived())
                .map(this::toDtoWithSubtaskCounts)
                .collect(Collectors.toList());
    }

    public List<TaskDto> listSubtasks(UUID userId, UUID taskId) {
        Task parent = findOrThrow(taskId);
        workspaceService.requireMembership(parent.getWorkspaceId(), userId);
        return taskRepository.findByParentTaskIdOrderByPositionAsc(taskId).stream()
                .filter(t -> !t.isArchived())
                .map(TaskDto::from)
                .collect(Collectors.toList());
    }

    public TaskDto get(UUID userId, UUID taskId) {
        Task task = findOrThrow(taskId);
        workspaceService.requireMembership(task.getWorkspaceId(), userId);
        return toDtoWithSubtaskCounts(task);
    }

    @Transactional
    public TaskDto update(UUID userId, UUID taskId, UpdateTaskRequest request) {
        Task task = findOrThrow(taskId);
        workspaceService.requireRoleAtLeast(task.getWorkspaceId(), userId, WorkspaceRole.EDITOR);

        UUID previousAssignee = task.getAssigneeId();
        TaskStatus previousStatus = task.getStatus();

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(parseStatus(request.getStatus()));
        }
        if (request.getPriority() != null) {
            task.setPriority(parsePriority(request.getPriority()));
        }
        if (request.getAssigneeId() != null) {
            task.setAssigneeId(request.getAssigneeId().isBlank() ? null : parseUuidOrNull(request.getAssigneeId()));
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate().isBlank() ? null : parseDateOrNull(request.getStartDate()));
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate().isBlank() ? null : parseDateOrNull(request.getDueDate()));
        }
        if (request.getPosition() != null) {
            task.setPosition(request.getPosition());
        }

        task = taskRepository.save(task);

        if (task.getAssigneeId() != null && !task.getAssigneeId().equals(previousAssignee)) {
            notificationService.notify(task.getAssigneeId(), userId, NotificationType.TASK_ASSIGNED,
                    "You were assigned to \"" + task.getTitle() + "\"", task.getWorkspaceId(), task.getId());
        }
        if (task.getStatus() == TaskStatus.DONE && previousStatus != TaskStatus.DONE) {
            activityLogService.log(task.getWorkspaceId(), userId, ActivityAction.COMPLETED, ActivityEntityType.TASK, task.getId(), task.getTitle());
        } else {
            activityLogService.log(task.getWorkspaceId(), userId, ActivityAction.EDITED, ActivityEntityType.TASK, task.getId(), task.getTitle());
        }

        return toDtoWithSubtaskCounts(task);
    }

    @Transactional
    public void delete(UUID userId, UUID taskId) {
        Task task = findOrThrow(taskId);
        workspaceService.requireRoleAtLeast(task.getWorkspaceId(), userId, WorkspaceRole.EDITOR);
        task.setArchived(true);
        taskRepository.save(task);
        activityLogService.log(task.getWorkspaceId(), userId, ActivityAction.DELETED, ActivityEntityType.TASK, task.getId(), task.getTitle());
        // Subtasks are archived too, so they don't linger as orphaned Kanban cards.
        for (Task sub : taskRepository.findByParentTaskIdOrderByPositionAsc(taskId)) {
            sub.setArchived(true);
            taskRepository.save(sub);
        }
    }

    private TaskDto toDtoWithSubtaskCounts(Task task) {
        TaskDto dto = TaskDto.from(task);
        List<Task> subtasks = taskRepository.findByParentTaskIdOrderByPositionAsc(task.getId()).stream()
                .filter(t -> !t.isArchived())
                .toList();
        dto.setSubtaskCount(subtasks.size());
        dto.setSubtaskDoneCount((int) subtasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count());
        return dto;
    }

    private TaskStatus parseStatus(String s) {
        try {
            return TaskStatus.valueOf(s.toUpperCase());
        } catch (Exception e) {
            throw ApiException.badRequest("Unknown task status: " + s);
        }
    }

    private TaskPriority parsePriority(String s) {
        if (s == null || s.isBlank()) return TaskPriority.MEDIUM;
        try {
            return TaskPriority.valueOf(s.toUpperCase());
        } catch (Exception e) {
            throw ApiException.badRequest("Unknown task priority: " + s);
        }
    }

    private UUID parseUuidOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return UUID.fromString(s);
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid ID: " + s);
        }
    }

    private LocalDate parseDateOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid date (expected YYYY-MM-DD): " + s);
        }
    }

    public Task findOrThrow(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> ApiException.notFound("Task not found"));
        if (task.isArchived()) {
            throw ApiException.notFound("Task not found");
        }
        return task;
    }
}
