package com.jorgelorza.clientes.pago.dto;

import com.jorgelorza.clientes.pago.model.MetodoPago;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Datos de entrada para registrar un pago.
 *
 * El monto no se recibe del cliente: se toma del precio del servicio asociado a la cita
 * para evitar manipulaciones. Solo el ADMIN puede invocar este endpoint.
 */
@Data
public class PagoRequest {

    @NotNull(message = "La cita es obligatoria")
    private Long citaId;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    /** Número de referencia o comprobante; opcional. */
    private String referencia;
}
