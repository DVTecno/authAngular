package com.auth.service.interfaces;

import com.auth.dto.response.auth.UserResponseDto;
import com.auth.model.User;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    void validateIdentity(Boolean identity, UUID userId);
    User findUserByIdOrDni(String userIdOrDni);
    User findUserById(UUID id);
    List<UserResponseDto> getAllUsers();
}
