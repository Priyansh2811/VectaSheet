package com.vectasheet.controller;

import com.vectasheet.dto.CellDto;
import com.vectasheet.dto.UpdateCellRequest;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.CellService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sheets/{sheetId}/cells")
public class CellController {

    private final CellService cellService;

    public CellController(CellService cellService) {
        this.cellService = cellService;
    }

    @GetMapping
    public ResponseEntity<List<CellDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID sheetId
    ) {
        return ResponseEntity.ok(cellService.listCells(principal.getId(), sheetId));
    }

    @PutMapping
    public ResponseEntity<List<CellDto>> updateCell(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID sheetId,
            @RequestBody UpdateCellRequest request
    ) {
        return ResponseEntity.ok(cellService.updateCell(principal.getId(), sheetId, request));
    }
}
