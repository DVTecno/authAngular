package com.auth.service.implementation;

import com.auth.config.mapper.UserMapper;
import com.auth.config.security.JwtService;
import com.auth.dto.request.auth.LoginRequestDto;
import com.auth.dto.request.auth.RegisterRequestDto;
import com.auth.dto.response.TokenValidationResponseDTO;
import com.auth.dto.response.auth.AuthResponseDto;
import com.auth.dto.response.auth.UserResponseDto;
import com.auth.exception.*;
import com.auth.model.Role;
import com.auth.model.TokenBlacklist;
import com.auth.model.User;
import com.auth.repository.IRoleRepository;
import com.auth.repository.IUserRepository;
import com.auth.repository.TokenBlacklistRepository;
import com.auth.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper mapper;
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    public AuthResponseDto login(LoginRequestDto dto) {
        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        } catch (Exception e) {
            throw new BadRequestException("Invalid username or password");
        }
        User user = userRepository.findUserByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException(String.format("User not found with email: %s",dto.getEmail())));

//        if (!Boolean.TRUE.equals(user.getActive())) {
//            throw new AccountActivationException("Tu cuenta no está activada. Por favor, activa tu cuenta.");
//        }

        return generateResponse(user);
    }

    @Transactional
    @Override
    public AuthResponseDto register(RegisterRequestDto dto) {
        Optional<User> userFound = userRepository.findUserByEmail(dto.getEmail());
        if(userFound.isPresent()) throw new BadRequestException(String.format("Email is already registered: %s",dto.getEmail()));
//        userFound = userRepository.findUserByDni(dto.getDni());
//        if(userFound.isPresent()) throw new BadRequestException(String.format("dni is already registered: %s",dto.getDni()));
        String roleName = dto.getUserType() ? "ROLE_COMPRADOR" : "ROLE_INVERSOR";
        Role role = roleRepository.findRoleByName(roleName).orElseThrow(() -> new NotFoundException(String.format("Role not found with name %s",roleName)));

        User newUser = new User();
        newUser.setEmail(dto.getEmail());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
//        newUser.setDni(dto.getDni().toUpperCase());
//        newUser.setName(dto.getName());
//        newUser.setLastname(dto.getLastname());
        newUser.getRoles().add(role);

        userRepository.save(newUser);
        return generateResponse(newUser);
    }

    @Override
    public AuthResponseDto checkLogin(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format("User not found with email: %s",email)));
        return generateResponse(user);
    }

    @Override
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User not found with ID: %s", userId)));
    }

    @Transactional
    @Override
    public void activateAccount(String token) {
        if (!jwtService.isActivationTokenValid(token)) {
            throw new AccountActivationException("El token de activación es inválido o ha expirado.");
        }
        String username = jwtService.getUsernameFromToken(token);
        User user = userRepository.findUserByEmail(username)
                .orElseThrow(() -> new AccountActivationException("No se encontró un usuario con el correo: " + username));
        if (Boolean.TRUE.equals(user.getActive())) {
            log.info("User {} already activated", username);
        }
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public String generateActivationToken(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new EmailServiceException("User not found with email: " + email));

        return jwtService.generateActivationToken(user.getUsername());
    }

    private AuthResponseDto generateResponse(User user) {
        UserResponseDto userR = mapper.toUserResponseDTO(user);

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthResponseDto(userR,token, refreshToken);
    }

    public Optional<String> getUserNameByEmail(String email) {
        Optional<User> userOptional = userRepository.findUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return Optional.ofNullable(user.getEmail());
        }
        return Optional.empty();
    }

    @Transactional
    public void updatePasswordToken(String token, String email) {
        Optional<User> optionalUsuario = userRepository.findUserByEmail(email);
        if (optionalUsuario.isPresent()) {
            User user = optionalUsuario.get();
            user.setResetPasswordToken(token);
            user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);
        } else {
            try {
                throw new EmailServiceException("User not found with email: " + email);
            } catch (EmailServiceException e) {
                throw new RuntimeException("Error processing password recovery request", e);
            }
        }
    }

    public User get(String resetPasswordToken) {
        if (resetPasswordToken == null) {
            try {
                throw new EmailServiceException("Token not provided.");
            } catch (EmailServiceException e) {
                throw new RuntimeException("Error al finalizar el restablecimiento de contraseña. Solicite un nuevo token.", e);
            }
        }
        Optional<User> optionalUsuario = userRepository.findByResetPasswordToken(resetPasswordToken);
        if (optionalUsuario.isPresent()) {
            return optionalUsuario.get();
        } else {
            try {
                throw new EmailServiceException("Invalid or expired token.");
            } catch (EmailServiceException e) {
                throw new RuntimeException("Error al finalizar el restablecimiento de contraseña. Solicite un nuevo token.", e);
            }
        }
    }

    @Transactional
    public void updatePassword(User user, String password) {
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setResetPasswordToken(null);
        userRepository.save(user);
    }

    public TokenValidationResponseDTO validateResetToken(String resetPasswordToken) {
        if (resetPasswordToken == null || resetPasswordToken.trim().isEmpty()) {
            throw new EmailServiceException("El token no fue proporcionado.");
        }
        Optional<User> optionalUser = userRepository.findByResetPasswordToken(resetPasswordToken);
        if (optionalUser.isEmpty()) {
            throw new EmailServiceException("El token es inválido o no se encuentra registrado.");
        }
        User user = optionalUser.get();
        if (user.getTokenExpiryDate() != null && user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new EmailServiceException("El token ha expirado. Solicite uno nuevo.");
        }
        return new TokenValidationResponseDTO("Token válido. Puede proceder a restablecer su contraseña.");
    }

    @Transactional
    @Override
    public AuthResponseDto refreshToken(String refreshToken) {
        if (!jwtService.isTokenExpired(refreshToken)) {
            throw new TokenExpiredException("El token de refresco ha expirado.", null);
        }

        String username = jwtService.getUsernameFromToken(refreshToken);

        User user = userRepository.findUserByEmail(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con el correo: " + username));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidTokenException("El token de refresco no coincide.", null);
        }

        String newToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new AuthResponseDto(mapper.toUserResponseDTO(user), newToken, newRefreshToken);
    }

    @Transactional
    @Override
    public void logout(String refreshToken, String accessToken) {
        String username = jwtService.getUsernameFromToken(refreshToken);

        User user = userRepository.findUserByEmail(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con el correo: " + username));

        user.setRefreshToken(null);
        userRepository.save(user);

        TokenBlacklist tokenBlacklist = new TokenBlacklist();
        tokenBlacklist.setToken(accessToken);
        tokenBlacklist.setExpiryDate(jwtService.getExpiration(accessToken));
        tokenBlacklistRepository.save(tokenBlacklist);
    }
}