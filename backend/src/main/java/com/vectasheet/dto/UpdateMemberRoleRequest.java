package com.vectasheet.dto;

import com.vectasheet.entity.WorkspaceRole;
import jakarta.validation.constraints.NotNull;

public class UpdateMemberRoleRequest {
    @NotNull
    private WorkspaceRole role;

    public WorkspaceRole getRole() { return role; }
    public void setRole(WorkspaceRole role) { this.role = role; }
}
