package com.jorgelorza.clientes.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta devuelta tanto en registro como en login.
 * El cliente debe incluir {@code token} en cada request posterior
 * como cabecera {@code Authorization: Bearer <token>}.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;         // JWT de acceso, corto plazo (jwt.expiration)
    private String refreshToken;  // Token de refresco, largo plazo (jwt.refresh-expiration)
    private String email;
    private String name;
    private String role;
}
