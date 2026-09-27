package com.vectasheet.controller;

import com.vectasheet.dto.FileAssetDto;
import com.vectasheet.security.UserPrincipal;
import com.vectasheet.service.FileService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/api/workspaces/{workspaceId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileAssetDto> upload(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(fileService.upload(principal.getId(), workspaceId, file));
    }

    @GetMapping("/api/workspaces/{workspaceId}/files")
    public ResponseEntity<List<FileAssetDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID workspaceId
    ) {
        return ResponseEntity.ok(fileService.list(principal.getId(), workspaceId));
    }

    @GetMapping("/api/files/{fileId}/download")
    public ResponseEntity<byte[]> download(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID fileId
    ) {
        FileService.DownloadableFile file = fileService.download(principal.getId(), fileId);
        ContentDisposition disposition = ContentDisposition.attachment().filename(file.filename()).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.bytes());
    }

    @DeleteMapping("/api/files/{fileId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID fileId
    ) {
        fileService.delete(principal.getId(), fileId);
        return ResponseEntity.noContent().build();
    }
}
