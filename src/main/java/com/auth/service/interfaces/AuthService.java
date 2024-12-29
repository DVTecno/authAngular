package com.auth.service.interfaces;

import com.auth.dto.request.auth.LoginRequestDto;
import com.auth.dto.request.auth.RegisterRequestDto;
import com.auth.dto.response.auth.AuthResponseDto;
import com.auth.model.User;

import java.util.UUID;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto dto);
    AuthResponseDto register(RegisterRequestDto dto);
    AuthResponseDto checkLogin(String email);
    User getUserById(UUID userId);
    void activateAccount(String token);
    String generateActivationToken(String email);
}
