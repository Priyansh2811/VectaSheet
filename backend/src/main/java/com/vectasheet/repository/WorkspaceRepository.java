package com.vectasheet.repository;

import com.vectasheet.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
}
