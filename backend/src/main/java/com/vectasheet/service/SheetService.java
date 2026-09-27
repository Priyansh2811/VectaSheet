package com.vectasheet.service;

import com.vectasheet.dto.SheetDto;
import com.vectasheet.entity.Sheet;
import com.vectasheet.entity.Spreadsheet;
import com.vectasheet.entity.WorkspaceRole;
import com.vectasheet.exception.ApiException;
import com.vectasheet.repository.CellRepository;
import com.vectasheet.repository.SheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SheetService {

    private final SheetRepository sheetRepository;
    private final CellRepository cellRepository;
    private final SpreadsheetService spreadsheetService;
    private final WorkspaceService workspaceService;

    public SheetService(
            SheetRepository sheetRepository,
            CellRepository cellRepository,
            SpreadsheetService spreadsheetService,
            WorkspaceService workspaceService
    ) {
        this.sheetRepository = sheetRepository;
        this.cellRepository = cellRepository;
        this.spreadsheetService = spreadsheetService;
        this.workspaceService = workspaceService;
    }

    public List<SheetDto> list(UUID userId, UUID spreadsheetId) {
        Spreadsheet spreadsheet = spreadsheetService.findOrThrow(spreadsheetId);
        workspaceService.requireMembership(spreadsheet.getWorkspaceId(), userId);
        return sheetRepository.findBySpreadsheetIdOrderByPositionAsc(spreadsheetId).stream()
                .map(SheetDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public SheetDto create(UUID userId, UUID spreadsheetId, String name) {
        Spreadsheet spreadsheet = spreadsheetService.findOrThrow(spreadsheetId);
        workspaceService.requireRoleAtLeast(spreadsheet.getWorkspaceId(), userId, WorkspaceRole.EDITOR);

        long existing = sheetRepository.countBySpreadsheetId(spreadsheetId);
        Sheet sheet = new Sheet();
        sheet.setSpreadsheetId(spreadsheetId);
        sheet.setName(name != null && !name.isBlank() ? name.trim() : "Sheet" + (existing + 1));
        sheet.setPosition((int) existing);
        sheet = sheetRepository.save(sheet);
        return SheetDto.from(sheet);
    }

    @Transactional
    public SheetDto rename(UUID userId, UUID sheetId, String newName) {
        Sheet sheet = findOrThrow(sheetId);
        Spreadsheet spreadsheet = spreadsheetService.findOrThrow(sheet.getSpreadsheetId());
        workspaceService.requireRoleAtLeast(spreadsheet.getWorkspaceId(), userId, WorkspaceRole.EDITOR);
        sheet.setName(newName.trim());
        sheet = sheetRepository.save(sheet);
        return SheetDto.from(sheet);
    }

    @Transactional
    public SheetDto duplicate(UUID userId, UUID sheetId) {
        Sheet source = findOrThrow(sheetId);
        Spreadsheet spreadsheet = spreadsheetService.findOrThrow(source.getSpreadsheetId());
        workspaceService.requireRoleAtLeast(spreadsheet.getWorkspaceId(), userId, WorkspaceRole.EDITOR);

        long existing = sheetRepository.countBySpreadsheetId(source.getSpreadsheetId());
        Sheet copy = new Sheet();
        copy.setSpreadsheetId(source.getSpreadsheetId());
        copy.setName(source.getName() + " copy");
        copy.setPosition((int) existing);
        copy.setRowCount(source.getRowCount());
        copy.setColCount(source.getColCount());
        copy = sheetRepository.save(copy);

        for (var cell : cellRepository.findBySheetId(source.getId())) {
            var newCell = new com.vectasheet.entity.Cell();
            newCell.setSheetId(copy.getId());
            newCell.setRowIndex(cell.getRowIndex());
            newCell.setColIndex(cell.getColIndex());
            newCell.setRawInput(cell.getRawInput());
            newCell.setComputedValue(cell.getComputedValue());
            newCell.setValueType(cell.getValueType());
            newCell.setModifiedBy(userId);
            cellRepository.save(newCell);
        }

        return SheetDto.from(copy);
    }

    @Transactional
    public void delete(UUID userId, UUID sheetId) {
        Sheet sheet = findOrThrow(sheetId);
        Spreadsheet spreadsheet = spreadsheetService.findOrThrow(sheet.getSpreadsheetId());
        workspaceService.requireRoleAtLeast(spreadsheet.getWorkspaceId(), userId, WorkspaceRole.EDITOR);

        long remaining = sheetRepository.countBySpreadsheetId(sheet.getSpreadsheetId());
        if (remaining <= 1) {
            throw ApiException.badRequest("A spreadsheet must have at least one sheet");
        }

        cellRepository.deleteBySheetId(sheetId);
        sheetRepository.delete(sheet);
    }

    public Sheet findOrThrow(UUID sheetId) {
        return sheetRepository.findById(sheetId)
                .orElseThrow(() -> ApiException.notFound("Sheet not found"));
    }
}
