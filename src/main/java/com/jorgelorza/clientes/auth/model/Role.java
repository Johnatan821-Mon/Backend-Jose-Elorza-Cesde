package com.jorgelorza.clientes.auth.model;

/**
 * Roles disponibles en el sistema.
 * El prefijo {@code ROLE_} es obligatorio para que Spring Security resuelva
 * correctamente las expresiones {@code hasRole('ADMIN')} en {@code @PreAuthorize}.
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
