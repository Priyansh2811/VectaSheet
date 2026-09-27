package com.vectasheet.repository;

import com.vectasheet.entity.Spreadsheet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpreadsheetRepository extends JpaRepository<Spreadsheet, UUID> {
    List<Spreadsheet> findByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId);
}
