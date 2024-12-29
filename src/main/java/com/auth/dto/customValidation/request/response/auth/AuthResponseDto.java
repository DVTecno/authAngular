package com.auth.dto.customValidation.request.response.auth;

public record AuthResponseDto(
        UserResponseDto user,
        String token
) {
}
