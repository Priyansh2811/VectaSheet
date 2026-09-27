package com.vectasheet.service;

import com.vectasheet.dto.FileAssetDto;
import com.vectasheet.entity.ActivityAction;
import com.vectasheet.entity.ActivityEntityType;
import com.vectasheet.entity.FileAsset;
import com.vectasheet.entity.WorkspaceRole;
import com.vectasheet.exception.ApiException;
import com.vectasheet.repository.FileAssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final FileAssetRepository repository;
    private final WorkspaceService workspaceService;
    private final ActivityLogService activityLogService;
    private final Path baseDir;
    private final long maxFileSizeBytes;

    public FileService(
            FileAssetRepository repository,
            WorkspaceService workspaceService,
            ActivityLogService activityLogService,
            @Value("${vectasheet.storage.base-dir:./data/uploads}") String baseDirPath,
            @Value("${vectasheet.storage.max-file-size-bytes:26214400}") long maxFileSizeBytes
    ) {
        this.repository = repository;
        this.workspaceService = workspaceService;
        this.activityLogService = activityLogService;
        this.baseDir = Paths.get(baseDirPath).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create file storage directory: " + this.baseDir, e);
        }
    }

    @Transactional
    public FileAssetDto upload(UUID userId, UUID workspaceId, MultipartFile file) {
        workspaceService.requireRoleAtLeast(workspaceId, userId, WorkspaceRole.EDITOR);

        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("No file was uploaded");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw ApiException.badRequest("File exceeds the maximum allowed size of " + (maxFileSizeBytes / (1024 * 1024)) + " MB");
        }

        String originalName = sanitizeDisplayName(file.getOriginalFilename());
        String storageFilename = UUID.randomUUID() + "-" + originalName;
        Path workspaceDir = baseDir.resolve(workspaceId.toString()).normalize();
        Path targetPath = workspaceDir.resolve(storageFilename).normalize();

        if (!targetPath.startsWith(baseDir)) {
            throw ApiException.badRequest("Invalid file name");
        }

        try {
            Files.createDirectories(workspaceDir);
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store uploaded file", e);
        }

        FileAsset asset = new FileAsset();
        asset.setWorkspaceId(workspaceId);
        asset.setOriginalFilename(originalName);
        asset.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        asset.setSizeBytes(file.getSize());
        asset.setStoragePath(workspaceId + "/" + storageFilename);
        asset.setUploadedBy(userId);
        asset = repository.save(asset);

        activityLogService.log(workspaceId, userId, ActivityAction.CREATED, ActivityEntityType.WORKSPACE, asset.getId(), asset.getOriginalFilename());

        return FileAssetDto.from(asset);
    }

    public List<FileAssetDto> list(UUID userId, UUID workspaceId) {
        workspaceService.requireMembership(workspaceId, userId);
        return repository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .filter(f -> !f.isArchived())
                .map(FileAssetDto::from)
                .collect(Collectors.toList());
    }

    public record DownloadableFile(byte[] bytes, String filename, String contentType) {}

    public DownloadableFile download(UUID userId, UUID fileId) {
        FileAsset asset = findOrThrow(fileId);
        workspaceService.requireMembership(asset.getWorkspaceId(), userId);

        Path path = baseDir.resolve(asset.getStoragePath()).normalize();
        if (!path.startsWith(baseDir) || !Files.exists(path)) {
            throw ApiException.notFound("File content is no longer available");
        }
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new DownloadableFile(bytes, asset.getOriginalFilename(), asset.getContentType());
        } catch (IOException e) {
            throw new IllegalStateException("Could not read stored file", e);
        }
    }

    @Transactional
    public void delete(UUID userId, UUID fileId) {
        FileAsset asset = findOrThrow(fileId);
        workspaceService.requireRoleAtLeast(asset.getWorkspaceId(), userId, WorkspaceRole.EDITOR);
        asset.setArchived(true);
        repository.save(asset);

        Path path = baseDir.resolve(asset.getStoragePath()).normalize();
        if (path.startsWith(baseDir)) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
        }
    }

    private String sanitizeDisplayName(String name) {
        if (name == null || name.isBlank()) return "file";
        String cleaned = Paths.get(name).getFileName().toString();
        return cleaned.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private FileAsset findOrThrow(UUID fileId) {
        FileAsset asset = repository.findById(fileId)
                .orElseThrow(() -> ApiException.notFound("File not found"));
        if (asset.isArchived()) throw ApiException.notFound("File not found");
        return asset;
    }
}
