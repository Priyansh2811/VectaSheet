package com.vectasheet.repository;

import com.vectasheet.entity.Sheet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SheetRepository extends JpaRepository<Sheet, UUID> {
    List<Sheet> findBySpreadsheetIdOrderByPositionAsc(UUID spreadsheetId);
    long countBySpreadsheetId(UUID spreadsheetId);
}
