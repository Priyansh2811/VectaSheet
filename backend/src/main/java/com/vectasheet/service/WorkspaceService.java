package com.vectasheet.service;

import com.vectasheet.dto.*;
import com.vectasheet.entity.*;
import com.vectasheet.exception.ApiException;
import com.vectasheet.repository.UserRepository;
import com.vectasheet.repository.WorkspaceMemberRepository;
import com.vectasheet.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;

    public WorkspaceService(
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository memberRepository,
            UserRepository userRepository
    ) {
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkspaceDto create(UUID userId, CreateWorkspaceRequest request) {
        Workspace workspace = new Workspace();
        workspace.setName(request.getName().trim());
        workspace.setDescription(request.getDescription());
        workspace.setOwnerId(userId);
        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspace.getId());
        member.setUserId(userId);
        member.setRole(WorkspaceRole.OWNER);
        memberRepository.save(member);

        return WorkspaceDto.from(workspace, WorkspaceRole.OWNER, 1);
    }

    public List<WorkspaceDto> listForUser(UUID userId) {
        List<WorkspaceMember> memberships = memberRepository.findByUserId(userId);
        return memberships.stream()
                .map(m -> {
                    Workspace w = workspaceRepository.findById(m.getWorkspaceId()).orElse(null);
                    if (w == null || w.isArchived()) return null;
                    int count = memberRepository.findByWorkspaceId(w.getId()).size();
                    return WorkspaceDto.from(w, m.getRole(), count);
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public WorkspaceDto get(UUID userId, UUID workspaceId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        WorkspaceRole role = requireMembership(workspaceId, userId);
        int count = memberRepository.findByWorkspaceId(workspaceId).size();
        return WorkspaceDto.from(workspace, role, count);
    }

    @Transactional
    public WorkspaceDto update(UUID userId, UUID workspaceId, UpdateWorkspaceRequest request) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        WorkspaceRole role = requireRoleAtLeast(workspaceId, userId, WorkspaceRole.ADMIN);

        if (request.getName() != null && !request.getName().isBlank()) {
            workspace.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }
        workspace = workspaceRepository.save(workspace);

        int count = memberRepository.findByWorkspaceId(workspaceId).size();
        return WorkspaceDto.from(workspace, role, count);
    }

    @Transactional
    public void delete(UUID userId, UUID workspaceId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        requireRoleAtLeast(workspaceId, userId, WorkspaceRole.OWNER);
        workspace.setArchived(true);
        workspaceRepository.save(workspace);
    }

    public List<MemberDto> listMembers(UUID userId, UUID workspaceId) {
        requireMembership(workspaceId, userId);
        return memberRepository.findByWorkspaceId(workspaceId).stream()
                .map(m -> {
                    User u = userRepository.findById(m.getUserId()).orElse(null);
                    if (u == null) return null;
                    return new MemberDto(u.getId(), u.getName(), u.getEmail(), u.getAvatarUrl(), m.getRole(), m.getJoinedAt());
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberDto inviteMember(UUID userId, UUID workspaceId, InviteMemberRequest request) {
        findWorkspaceOrThrow(workspaceId);
        requireRoleAtLeast(workspaceId, userId, WorkspaceRole.ADMIN);

        if (request.getRole() == WorkspaceRole.OWNER) {
            throw ApiException.badRequest("Cannot invite a member directly as Owner");
        }

        User invitedUser = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> ApiException.notFound("No VectaSheet account found for that email"));

        if (memberRepository.findByWorkspaceIdAndUserId(workspaceId, invitedUser.getId()).isPresent()) {
            throw ApiException.conflict("This person is already a member of the workspace");
        }

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspaceId);
        member.setUserId(invitedUser.getId());
        member.setRole(request.getRole());
        member = memberRepository.save(member);

        return new MemberDto(invitedUser.getId(), invitedUser.getName(), invitedUser.getEmail(),
                invitedUser.getAvatarUrl(), member.getRole(), member.getJoinedAt());
    }

    @Transactional
    public MemberDto updateMemberRole(UUID userId, UUID workspaceId, UUID targetUserId, UpdateMemberRoleRequest request) {
        requireRoleAtLeast(workspaceId, userId, WorkspaceRole.ADMIN);

        WorkspaceMember member = memberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> ApiException.notFound("Member not found in this workspace"));

        if (member.getRole() == WorkspaceRole.OWNER) {
            throw ApiException.forbidden("Cannot change the role of the workspace owner");
        }
        if (request.getRole() == WorkspaceRole.OWNER) {
            throw ApiException.badRequest("Ownership transfer is not supported here");
        }

        member.setRole(request.getRole());
        member = memberRepository.save(member);

        User u = userRepository.findById(targetUserId).orElseThrow();
        return new MemberDto(u.getId(), u.getName(), u.getEmail(), u.getAvatarUrl(), member.getRole(), member.getJoinedAt());
    }

    @Transactional
    public void removeMember(UUID userId, UUID workspaceId, UUID targetUserId) {
        requireRoleAtLeast(workspaceId, userId, WorkspaceRole.ADMIN);

        WorkspaceMember member = memberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> ApiException.notFound("Member not found in this workspace"));

        if (member.getRole() == WorkspaceRole.OWNER) {
            throw ApiException.forbidden("Cannot remove the workspace owner");
        }

        memberRepository.deleteByWorkspaceIdAndUserId(workspaceId, targetUserId);
    }

    // --- permission helpers (also used by other module services, e.g. spreadsheets) ---

    private Workspace findWorkspaceOrThrow(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> ApiException.notFound("Workspace not found"));
        if (workspace.isArchived()) {
            throw ApiException.notFound("Workspace not found");
        }
        return workspace;
    }

    /** Throws if the user is not a member of the workspace; otherwise returns their role. */
    public WorkspaceRole requireMembership(UUID workspaceId, UUID userId) {
        return memberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(WorkspaceMember::getRole)
                .orElseThrow(() -> ApiException.forbidden("You don't have access to this workspace"));
    }

    /** Throws unless the user's role in the workspace is at or above the given minimum. */
    public WorkspaceRole requireRoleAtLeast(UUID workspaceId, UUID userId, WorkspaceRole minimum) {
        WorkspaceRole role = requireMembership(workspaceId, userId);
        if (rank(role) < rank(minimum)) {
            throw ApiException.forbidden("You don't have permission to perform this action");
        }
        return role;
    }

    private int rank(WorkspaceRole role) {
        return switch (role) {
            case VIEWER -> 0;
            case COMMENTER -> 1;
            case EDITOR -> 2;
            case ADMIN -> 3;
            case OWNER -> 4;
        };
    }
}
