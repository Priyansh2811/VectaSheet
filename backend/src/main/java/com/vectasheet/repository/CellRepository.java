package com.vectasheet.repository;

import com.vectasheet.entity.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CellRepository extends JpaRepository<Cell, UUID> {
    List<Cell> findBySheetId(UUID sheetId);
    Optional<Cell> findBySheetIdAndRowIndexAndColIndex(UUID sheetId, int rowIndex, int colIndex);
    void deleteBySheetId(UUID sheetId);
}
