package com.vectasheet.controller;

import com.vectasheet.dto.CreateSpreadsheetRequest;
import com.vectasheet.dto.SpreadsheetDto;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.SpreadsheetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class SpreadsheetController {

    private final SpreadsheetService spreadsheetService;

    public SpreadsheetController(SpreadsheetService spreadsheetService) {
        this.spreadsheetService = spreadsheetService;
    }

    @PostMapping("/api/workspaces/{workspaceId}/spreadsheets")
    public ResponseEntity<SpreadsheetDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateSpreadsheetRequest request
    ) {
        return ResponseEntity.ok(spreadsheetService.create(principal.getId(), workspaceId, request));
    }

    @GetMapping("/api/workspaces/{workspaceId}/spreadsheets")
    public ResponseEntity<List<SpreadsheetDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId
    ) {
        return ResponseEntity.ok(spreadsheetService.list(principal.getId(), workspaceId));
    }

    @GetMapping("/api/spreadsheets/{spreadsheetId}")
    public ResponseEntity<SpreadsheetDto> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID spreadsheetId
    ) {
        return ResponseEntity.ok(spreadsheetService.get(principal.getId(), spreadsheetId));
    }

    @PatchMapping("/api/spreadsheets/{spreadsheetId}")
    public ResponseEntity<SpreadsheetDto> rename(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID spreadsheetId,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(spreadsheetService.rename(principal.getId(), spreadsheetId, body.get("name")));
    }

    @DeleteMapping("/api/spreadsheets/{spreadsheetId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID spreadsheetId
    ) {
        spreadsheetService.delete(principal.getId(), spreadsheetId);
        return ResponseEntity.noContent().build();
    }
}
