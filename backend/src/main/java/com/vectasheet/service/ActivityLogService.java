package com.vectasheet.service;

import com.vectasheet.dto.ActivityLogDto;
import com.vectasheet.entity.ActivityAction;
import com.vectasheet.entity.ActivityEntityType;
import com.vectasheet.entity.ActivityLog;
import com.vectasheet.repository.ActivityLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A lightweight, append-only activity feed. Other services call log(...) after a
 * meaningful change; this phase wires it into workspace membership, tasks, and
 * documents. Extending it to every entity type is mechanical — call log() at the
 * point of each write — but hasn't been done exhaustively yet (see README).
 */
@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;
    private final WorkspaceService workspaceService;

    public ActivityLogService(ActivityLogRepository repository, WorkspaceService workspaceService) {
        this.repository = repository;
        this.workspaceService = workspaceService;
    }

    @Transactional
    public void log(UUID workspaceId, UUID actorId, ActivityAction action, ActivityEntityType entityType, UUID entityId, String entityLabel) {
        ActivityLog entry = new ActivityLog();
        entry.setWorkspaceId(workspaceId);
        entry.setActorId(actorId);
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setEntityLabel(entityLabel);
        repository.save(entry);
    }

    public List<ActivityLogDto> list(UUID userId, UUID workspaceId, int limit) {
        workspaceService.requireMembership(workspaceId, userId);
        return repository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId, PageRequest.of(0, Math.min(limit, 200)))
                .stream()
                .map(ActivityLogDto::from)
                .collect(Collectors.toList());
    }
}
