package com.vectasheet.controller;

import com.vectasheet.dto.SearchResultDto;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/api/workspaces/{workspaceId}/search")
    public ResponseEntity<List<SearchResultDto>> search(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId,
            @RequestParam("q") String query
    ) {
        return ResponseEntity.ok(searchService.search(principal.getId(), workspaceId, query));
    }
}
