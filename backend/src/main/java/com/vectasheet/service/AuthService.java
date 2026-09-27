package com.vectasheet.service;

import com.vectasheet.dto.*;
import com.vectasheet.entity.PasswordResetToken;
import com.vectasheet.entity.RefreshToken;
import com.vectasheet.entity.User;
import com.vectasheet.entity.Workspace;
import com.vectasheet.entity.WorkspaceMember;
import com.vectasheet.entity.WorkspaceRole;
import com.vectasheet.exception.ApiException;
import com.vectasheet.repository.PasswordResetTokenRepository;
import com.vectasheet.repository.RefreshTokenRepository;
import com.vectasheet.repository.UserRepository;
import com.vectasheet.repository.WorkspaceMemberRepository;
import com.vectasheet.repository.WorkspaceRepository;
import com.vectasheet.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private static final long REFRESH_TOKEN_TTL_DAYS = 30;
    private static final long RESET_TOKEN_TTL_MINUTES = 30;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PasswordResetTokenRepository passwordResetTokenRepository
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw ApiException.conflict("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);
        user = userRepository.save(user);

        // Every new user gets a personal starter workspace so the app isn't empty on first login.
        Workspace workspace = new Workspace();
        workspace.setName(user.getName() + "'s Workspace");
        workspace.setDescription("Your personal workspace to get started.");
        workspace.setOwnerId(user.getId());
        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspace.getId());
        member.setUserId(user.getId());
        member.setRole(WorkspaceRole.OWNER);
        workspaceMemberRepository.save(member);

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid email or password");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.unauthorized("Refresh token expired, please log in again");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> ApiException.unauthorized("Account no longer exists"));

        // Rotate: revoke the old refresh token and issue a brand new pair.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(user);
    }

    @Transactional
    public void logout(UUID userId, String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
            if (rt.getUserId().equals(userId)) {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            }
        });
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate all existing sessions on password change.
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always behave the same whether the email exists or not, to avoid leaking account existence.
        userRepository.findByEmailIgnoreCase(request.getEmail().trim()).ifPresent(user -> {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setUserId(user.getId());
            resetToken.setExpiresAt(Instant.now().plus(RESET_TOKEN_TTL_MINUTES, ChronoUnit.MINUTES));
            passwordResetTokenRepository.save(resetToken);

            // No email provider is configured in this local setup, so the reset link is logged
            // instead of silently pretending an email was sent. Wire a real provider (e.g. SES,
            // Postmark) in EmailService before production use.
            System.out.println("============================================");
            System.out.println(" Password reset requested for " + user.getEmail());
            System.out.println(" Reset link: http://localhost:5173/reset-password?token=" + resetToken.getToken());
            System.out.println(" Expires in " + RESET_TOKEN_TTL_MINUTES + " minutes");
            System.out.println("============================================");
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> ApiException.badRequest("This reset link is invalid or has expired"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.badRequest("This reset link is invalid or has expired");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> ApiException.badRequest("This reset link is invalid or has expired"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.deleteByUserId(user.getId());
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString() + "." + UUID.randomUUID());
        refreshToken.setUserId(user.getId());
        refreshToken.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_TTL_DAYS, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshToken.getToken(), UserDto.from(user));
    }
}
