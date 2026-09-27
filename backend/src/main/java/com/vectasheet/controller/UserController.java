package com.vectasheet.controller;

import com.vectasheet.dto.UserDto;
import com.vectasheet.entity.User;
import com.vectasheet.exception.ApiException;
import com.vectasheet.repository.UserRepository;
import com.vectasheet.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> ApiException.notFound("User not found"));
        return ResponseEntity.ok(UserDto.from(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateProfileRequest request
    ) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> ApiException.notFound("User not found"));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        user = userRepository.save(user);
        return ResponseEntity.ok(UserDto.from(user));
    }

    public record UpdateProfileRequest(String name, String avatarUrl) {}
}
