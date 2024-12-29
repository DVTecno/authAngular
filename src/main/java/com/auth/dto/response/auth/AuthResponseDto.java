package com.auth.dto.response.auth;

public record AuthResponseDto(
        UserResponseDto user,
        String token
) {
}
