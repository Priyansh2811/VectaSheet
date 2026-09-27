package com.vectasheet.controller;

import com.vectasheet.dto.CreateSheetRequest;
import com.vectasheet.dto.RenameSheetRequest;
import com.vectasheet.dto.SheetDto;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.SheetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class SheetController {

    private final SheetService sheetService;

    public SheetController(SheetService sheetService) {
        this.sheetService = sheetService;
    }

    @GetMapping("/api/spreadsheets/{spreadsheetId}/sheets")
    public ResponseEntity<List<SheetDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID spreadsheetId
    ) {
        return ResponseEntity.ok(sheetService.list(principal.getId(), spreadsheetId));
    }

    @PostMapping("/api/spreadsheets/{spreadsheetId}/sheets")
    public ResponseEntity<SheetDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID spreadsheetId,
            @RequestBody CreateSheetRequest request
    ) {
        return ResponseEntity.ok(sheetService.create(principal.getId(), spreadsheetId, request.getName()));
    }

    @PatchMapping("/api/sheets/{sheetId}")
    public ResponseEntity<SheetDto> rename(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID sheetId,
            @Valid @RequestBody RenameSheetRequest request
    ) {
        return ResponseEntity.ok(sheetService.rename(principal.getId(), sheetId, request.getName()));
    }

    @PostMapping("/api/sheets/{sheetId}/duplicate")
    public ResponseEntity<SheetDto> duplicate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID sheetId
    ) {
        return ResponseEntity.ok(sheetService.duplicate(principal.getId(), sheetId));
    }

    @DeleteMapping("/api/sheets/{sheetId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID sheetId
    ) {
        sheetService.delete(principal.getId(), sheetId);
        return ResponseEntity.noContent().build();
    }
}
