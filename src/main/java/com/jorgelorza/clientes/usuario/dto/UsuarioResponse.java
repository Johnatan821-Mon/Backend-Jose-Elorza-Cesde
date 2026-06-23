package com.jorgelorza.clientes.usuario.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Datos del usuario expuestos al exterior.
 *
 * No incluye la contraseña ni ningún dato interno de Spring Security.
 * {@code role} se serializa como String (p.ej. "ROLE_ADMIN") para que el cliente
 * no dependa del enum {@link com.jorgelorza.clientes.usuario.model.Role}.
 */
@Data
@Builder
public class UsuarioResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private boolean active;
}
