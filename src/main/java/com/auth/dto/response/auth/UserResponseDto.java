package com.auth.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Set;
import java.util.UUID;

@JsonPropertyOrder({"userId", "email", "name", "lastname", "dni", "isVerified", "roles"})
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