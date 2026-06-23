package com.jorgelorza.clientes.auth.service;

import com.jorgelorza.clientes.auth.dto.AuthResponse;
import com.jorgelorza.clientes.auth.dto.LoginRequest;
import com.jorgelorza.clientes.auth.dto.RegisterRequest;
import com.jorgelorza.clientes.auth.model.RefreshToken;
import com.jorgelorza.clientes.auth.security.JwtUtil;
import com.jorgelorza.clientes.usuario.model.Role;
import com.jorgelorza.clientes.usuario.model.User;
import com.jorgelorza.clientes.usuario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestiona el registro, login, refresco de token y logout.
 * No maneja sesiones — la autenticación se basa en un access token JWT de corta
 * duración y un refresh token almacenado en BD para poder invalidarlo en logout.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    /**
     * Registra un nuevo usuario con rol {@code ROLE_USER} por defecto.
     * La contraseña se hashea con BCrypt antes de persistirse.
     *
     * @throws IllegalArgumentException si el email ya existe en la BD.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);
        return buildResponse(user);
    }

    /**
     * Autentica credenciales contra la BD y devuelve access + refresh token si son válidas.
     *
     * {@code authenticationManager.authenticate} lanza {@code BadCredentialsException}
     * si el email no existe o la contraseña no coincide.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        return buildResponse(user);
    }

    /**
     * Valida el refresh token, emite un nuevo access token y rota el refresh token
     * (el token usado queda invalidado y se emite uno nuevo).
     *
     * @throws IllegalArgumentException si el refresh token no existe o expiró.
     */
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenService.validate(refreshToken);
        User user = stored.getUser();

        // Rotación: invalida el refresh token usado y emite uno nuevo
        RefreshToken newRefreshToken = refreshTokenService.create(user);

        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user))
                .refreshToken(newRefreshToken.getToken())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Invalida el refresh token del usuario en BD.
     * El access token expirará solo según su tiempo de vida configurado.
     *
     * @throws IllegalArgumentException si el refresh token no existe.
     */
    @Transactional
    public void logout(String refreshToken) {
        RefreshToken stored = refreshTokenService.validate(refreshToken);
        refreshTokenService.deleteByUser(stored.getUser());
    }

    /** Construye el DTO de respuesta generando access token y refresh token nuevos. */
    private AuthResponse buildResponse(User user) {
        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user))
                .refreshToken(refreshTokenService.create(user).getToken())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}
