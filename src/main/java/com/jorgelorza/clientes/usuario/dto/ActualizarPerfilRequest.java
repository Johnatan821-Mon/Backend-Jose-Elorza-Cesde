package com.jorgelorza.clientes.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Datos que un usuario puede modificar de su propio perfil.
 *
 * El email y el rol no son editables por el propio usuario;
 * solo un administrador podría cambiar el rol directamente en BD.
 */
@Data
public class ActualizarPerfilRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    /** Opcional: si no se envía, se guarda {@code null} y se borra el teléfono anterior. */
    private String phone;
}
