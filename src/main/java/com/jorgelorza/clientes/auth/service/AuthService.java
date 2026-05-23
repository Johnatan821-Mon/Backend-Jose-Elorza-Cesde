package com.jorgelorza.clientes.auth.service;

import com.jorgelorza.clientes.auth.dto.AuthResponse;
import com.jorgelorza.clientes.auth.dto.LoginRequest;
import com.jorgelorza.clientes.auth.dto.RegisterRequest;
import com.jorgelorza.clientes.auth.model.Role;
import com.jorgelorza.clientes.auth.model.User;
import com.jorgelorza.clientes.auth.repository.UserRepository;
import com.jorgelorza.clientes.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Gestiona el registro y el login de usuarios.
 * No maneja sesiones — cada operación exitosa devuelve un JWT que el cliente
 * debe conservar y adjuntar en requests posteriores.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Registra un nuevo usuario con rol {@code ROLE_USER} por defecto.
     * La contraseña se hashea con BCrypt antes de persistirse.
     *
     * @throws IllegalArgumentException si el email ya existe en la BD
     */
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
     * Autentica credenciales contra la BD y devuelve un JWT si son válidas.
     *
     * {@code authenticationManager.authenticate} lanza {@code BadCredentialsException}
     * si el email no existe o la contraseña no coincide, lo que Spring Security
     * convierte en 401 automáticamente.
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        return buildResponse(user);
    }

    /** Construye el DTO de respuesta común para registro y login. */
    private AuthResponse buildResponse(User user) {
        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user))
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}
