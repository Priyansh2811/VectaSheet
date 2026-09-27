package com.vectasheet.repository;

import com.vectasheet.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {
    List<WorkspaceMember> findByUserId(UUID userId);
    List<WorkspaceMember> findByWorkspaceId(UUID workspaceId);
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
    void deleteByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
}
