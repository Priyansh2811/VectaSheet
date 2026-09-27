package com.vectasheet.service;

import com.vectasheet.dto.SearchResultDto;
import com.vectasheet.repository.DocumentRepository;
import com.vectasheet.repository.SpreadsheetRepository;
import com.vectasheet.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SearchService {

    private final DocumentRepository documentRepository;
    private final SpreadsheetRepository spreadsheetRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceService workspaceService;

    public SearchService(
            DocumentRepository documentRepository,
            SpreadsheetRepository spreadsheetRepository,
            TaskRepository taskRepository,
            WorkspaceService workspaceService
    ) {
        this.documentRepository = documentRepository;
        this.spreadsheetRepository = spreadsheetRepository;
        this.taskRepository = taskRepository;
        this.workspaceService = workspaceService;
    }

    public List<SearchResultDto> search(UUID userId, UUID workspaceId, String query) {
        workspaceService.requireMembership(workspaceId, userId);
        List<SearchResultDto> results = new ArrayList<>();
        if (query == null || query.isBlank()) return results;
        String q = query.trim().toLowerCase();

        documentRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId).stream()
                .filter(d -> !d.isArchived())
                .filter(d -> d.getTitle() != null && d.getTitle().toLowerCase().contains(q))
                .forEach(d -> results.add(new SearchResultDto("DOCUMENT", d.getId(), d.getTitle(), "Document")));

        spreadsheetRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .filter(s -> !s.isArchived())
                .filter(s -> s.getName() != null && s.getName().toLowerCase().contains(q))
                .forEach(s -> results.add(new SearchResultDto("SPREADSHEET", s.getId(), s.getName(), "Spreadsheet")));

        taskRepository.findByWorkspaceIdAndParentTaskIdIsNullOrderByPositionAsc(workspaceId).stream()
                .filter(t -> !t.isArchived())
                .filter(t -> t.getTitle() != null && t.getTitle().toLowerCase().contains(q))
                .forEach(t -> results.add(new SearchResultDto("TASK", t.getId(), t.getTitle(), "Task \u00b7 " + t.getStatus())));

        return results;
    }
}
