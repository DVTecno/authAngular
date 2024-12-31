package com.auth.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

@JsonPropertyOrder({"roleId", "name"})
public record RoleResponseDto(UUID roleId,String name) {
}