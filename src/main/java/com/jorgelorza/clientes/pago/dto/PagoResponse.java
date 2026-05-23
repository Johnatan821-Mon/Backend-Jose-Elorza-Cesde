package com.jorgelorza.clientes.pago.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Proyección del pago devuelta al cliente.
 * Incluye datos del usuario y del servicio desnormalizados para evitar
 * llamadas adicionales desde el frontend al mostrar el historial de pagos.
 */
@Data
@Builder
public class PagoResponse {

    private Long id;
    private Long citaId;
    private String usuarioNombre;
    private String servicioNombre;
    private BigDecimal monto;
    private String estado;
    private String metodoPago;
    private String referencia;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaCreacion;
}
