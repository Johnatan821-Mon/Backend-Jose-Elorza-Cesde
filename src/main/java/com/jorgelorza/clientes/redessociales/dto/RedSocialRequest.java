package com.jorgelorza.clientes.redessociales.dto;

import com.jorgelorza.clientes.redessociales.model.TipoRedSocial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Datos de entrada para crear o actualizar un perfil de red social. */
@Data
public class RedSocialRequest {

    @NotNull(message = "El tipo de red social es obligatorio")
    private TipoRedSocial tipo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La URL es obligatoria")
    private String url;
}
