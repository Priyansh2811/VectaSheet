package com.vectasheet.repository;

import com.vectasheet.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByWorkspaceIdAndParentTaskIdIsNullOrderByPositionAsc(UUID workspaceId);
    List<Task> findByParentTaskIdOrderByPositionAsc(UUID parentTaskId);
    long countByWorkspaceIdAndStatus(UUID workspaceId, com.vectasheet.entity.TaskStatus status);
}
