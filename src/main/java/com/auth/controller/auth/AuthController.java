package com.auth.controller.auth;

import com.auth.config.CurrentUser;
import com.auth.dto.request.auth.EmailRequestDto;
import com.auth.dto.request.auth.LoginRequestDto;
import com.auth.dto.request.auth.RefreshTokenRequest;
import com.auth.dto.request.auth.RegisterRequestDto;
import com.auth.dto.response.auth.AuthResponseDto;
import com.auth.model.User;
import com.auth.service.interfaces.AuthService;
import com.auth.service.interfaces.IEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IEmailService emailService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login (@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok().body(authService.login(dto));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register (@Valid @RequestBody RegisterRequestDto dto) {
        AuthResponseDto response = authService.register(dto);
        emailService.sendAccountActivationEmail(dto.getEmail());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/check-login")
    public ResponseEntity<AuthResponseDto> checkLogin (@CurrentUser User user) {

        return ResponseEntity.ok().body(authService.checkLogin(user.getEmail()));
    }

    @GetMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestParam("token") String token) {
        authService.activateAccount(token);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("https://login-clase.up.railway.app/dashboard"))
                .build();
    }

    @PostMapping("/generate-token")
    public ResponseEntity<String> generateActivationToken(@RequestBody @Valid EmailRequestDto emailRequestDto) {
        emailService.sendAccountActivationEmail(emailRequestDto.email());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Token de activación generado y enviado al correo.");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(@RequestBody @Valid RefreshTokenRequest request)  {
        return ResponseEntity.ok(authService.refreshToken(request.refreshToken()));
    }
}
