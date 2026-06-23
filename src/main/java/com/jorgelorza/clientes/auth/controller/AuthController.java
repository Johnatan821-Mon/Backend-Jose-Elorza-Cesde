package com.jorgelorza.clientes.auth.controller;

import com.jorgelorza.clientes.auth.dto.AuthResponse;
import com.jorgelorza.clientes.auth.dto.LoginRequest;
import com.jorgelorza.clientes.auth.dto.RefreshRequest;
import com.jorgelorza.clientes.auth.dto.RegisterRequest;
import com.jorgelorza.clientes.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints públicos de autenticación — no requieren JWT.
 * Declarados como permitAll en {@link com.jorgelorza.clientes.auth.security.SecurityConfig}.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Crea una cuenta nueva y devuelve access token + refresh token.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * POST /api/auth/login
     * Valida credenciales y devuelve access token + refresh token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/refresh
     * Emite un nuevo access token a partir de un refresh token válido.
     * El refresh token usado se invalida y se devuelve uno nuevo (rotación).
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    /**
     * POST /api/auth/logout
     * Invalida el refresh token en BD. El access token expirará por su propio TTL.
     * Devuelve 204 No Content — el cliente debe descartar ambos tokens localmente.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
