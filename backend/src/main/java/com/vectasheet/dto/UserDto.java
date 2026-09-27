package com.vectasheet.dto;

import com.vectasheet.entity.User;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String name;
    private String email;
    private String avatarUrl;
    private boolean emailVerified;

    public static UserDto from(User u) {
        UserDto dto = new UserDto();
        dto.id = u.getId();
        dto.name = u.getName();
        dto.email = u.getEmail();
        dto.avatarUrl = u.getAvatarUrl();
        dto.emailVerified = u.isEmailVerified();
        return dto;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isEmailVerified() { return emailVerified; }
}
