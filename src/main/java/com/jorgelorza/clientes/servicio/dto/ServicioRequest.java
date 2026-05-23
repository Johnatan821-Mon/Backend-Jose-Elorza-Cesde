package com.jorgelorza.clientes.servicio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Datos de entrada para crear o actualizar un servicio.
 * Usado tanto en POST (crear) como en PUT (actualizar completo).
 */
@Data
public class ServicioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 5, message = "La duración mínima es 5 minutos")
    private Integer duracionMinutos;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio debe ser positivo")
    private BigDecimal precio;
}
