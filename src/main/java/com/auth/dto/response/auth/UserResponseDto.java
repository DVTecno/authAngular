package com.auth.dto.response.auth;

import java.util.Set;
import java.util.UUID;

public record UserResponseDto(
        UUID userId,
        String email,
        String name,
        String lastname,
        String dni,
        Boolean isVerified,
        Set<RoleResponseDto> roles
) {
}
