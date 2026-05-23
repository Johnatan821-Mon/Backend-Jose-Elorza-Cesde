package com.jorgelorza.clientes.auth.controller;

import com.jorgelorza.clientes.auth.dto.AuthResponse;
import com.jorgelorza.clientes.auth.dto.LoginRequest;
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
     * Crea una cuenta nueva y devuelve un JWT listo para usar.
     * Retorna 201 Created para indicar que se creó un recurso nuevo.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * POST /api/auth/login
     * Valida credenciales y devuelve un JWT si son correctas.
     * Retorna 200 OK porque no crea ningún recurso, solo autentica.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
