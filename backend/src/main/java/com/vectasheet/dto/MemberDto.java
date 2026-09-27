package com.vectasheet.dto;

import com.vectasheet.entity.WorkspaceRole;
import java.time.Instant;
import java.util.UUID;

public class MemberDto {
    private UUID userId;
    private String name;
    private String email;
    private String avatarUrl;
    private WorkspaceRole role;
    private Instant joinedAt;

    public MemberDto(UUID userId, String name, String email, String avatarUrl, WorkspaceRole role, Instant joinedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
    public WorkspaceRole getRole() { return role; }
    public Instant getJoinedAt() { return joinedAt; }
}
