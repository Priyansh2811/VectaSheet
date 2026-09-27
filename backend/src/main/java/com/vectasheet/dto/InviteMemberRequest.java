package com.vectasheet.dto;

import com.vectasheet.entity.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InviteMemberRequest {
    @NotBlank
    @Email
    private String email;

    @NotNull
    private WorkspaceRole role;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public WorkspaceRole getRole() { return role; }
    public void setRole(WorkspaceRole role) { this.role = role; }
}
