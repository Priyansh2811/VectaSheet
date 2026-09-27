package com.vectasheet.service;

import com.vectasheet.dto.CreateSpreadsheetRequest;
import com.vectasheet.dto.SpreadsheetDto;
import com.vectasheet.entity.Sheet;
import com.vectasheet.entity.Spreadsheet;
import com.vectasheet.entity.WorkspaceRole;
import com.vectasheet.exception.ApiException;
import com.vectasheet.repository.SheetRepository;
import com.vectasheet.repository.SpreadsheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SpreadsheetService {

    private final SpreadsheetRepository spreadsheetRepository;
    private final SheetRepository sheetRepository;
    private final WorkspaceService workspaceService;

    public SpreadsheetService(
            SpreadsheetRepository spreadsheetRepository,
            SheetRepository sheetRepository,
            WorkspaceService workspaceService
    ) {
        this.spreadsheetRepository = spreadsheetRepository;
        this.sheetRepository = sheetRepository;
        this.workspaceService = workspaceService;
    }

    @Transactional
    public SpreadsheetDto create(UUID userId, UUID workspaceId, CreateSpreadsheetRequest request) {
        workspaceService.requireRoleAtLeast(workspaceId, userId, WorkspaceRole.EDITOR);

        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setWorkspaceId(workspaceId);
        spreadsheet.setName(request.getName().trim());
        spreadsheet.setCreatedBy(userId);
        spreadsheet = spreadsheetRepository.save(spreadsheet);

        // Every new spreadsheet starts with one sheet, like a real spreadsheet app.
        Sheet sheet = new Sheet();
        sheet.setSpreadsheetId(spreadsheet.getId());
        sheet.setName("Sheet1");
        sheet.setPosition(0);
        sheetRepository.save(sheet);

        return SpreadsheetDto.from(spreadsheet);
    }

    public List<SpreadsheetDto> list(UUID userId, UUID workspaceId) {
        workspaceService.requireMembership(workspaceId, userId);
        return spreadsheetRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .filter(s -> !s.isArchived())
                .map(SpreadsheetDto::from)
                .collect(Collectors.toList());
    }

    public SpreadsheetDto get(UUID userId, UUID spreadsheetId) {
        Spreadsheet spreadsheet = findOrThrow(spreadsheetId);
        workspaceService.requireMembership(spreadsheet.getWorkspaceId(), userId);
        return SpreadsheetDto.from(spreadsheet);
    }

    @Transactional
    public SpreadsheetDto rename(UUID userId, UUID spreadsheetId, String newName) {
        Spreadsheet spreadsheet = findOrThrow(spreadsheetId);
        workspaceService.requireRoleAtLeast(spreadsheet.getWorkspaceId(), userId, WorkspaceRole.EDITOR);
        spreadsheet.setName(newName.trim());
        spreadsheet = spreadsheetRepository.save(spreadsheet);
        return SpreadsheetDto.from(spreadsheet);
    }

    @Transactional
    public void delete(UUID userId, UUID spreadsheetId) {
        Spreadsheet spreadsheet = findOrThrow(spreadsheetId);
        workspaceService.requireRoleAtLeast(spreadsheet.getWorkspaceId(), userId, WorkspaceRole.ADMIN);
        spreadsheet.setArchived(true);
        spreadsheetRepository.save(spreadsheet);
    }

    /** Used by SheetService/CellController to authorize access via the sheet's parent spreadsheet. */
    public Spreadsheet findOrThrow(UUID spreadsheetId) {
        Spreadsheet spreadsheet = spreadsheetRepository.findById(spreadsheetId)
                .orElseThrow(() -> ApiException.notFound("Spreadsheet not found"));
        if (spreadsheet.isArchived()) {
            throw ApiException.notFound("Spreadsheet not found");
        }
        return spreadsheet;
    }
}
