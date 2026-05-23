package com.jorgelorza.clientes.servicio.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Proyección del servicio devuelta al cliente.
 * Incluye {@code activo} para que el ADMIN pueda distinguir servicios desactivados
 * al consultar el listado completo ({@code /api/servicios/todos}).
 */
@Data
@Builder
public class ServicioResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private BigDecimal precio;
    private boolean activo;
}
