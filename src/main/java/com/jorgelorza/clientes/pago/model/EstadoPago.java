package com.jorgelorza.clientes.pago.model;

/**
 * Estados posibles de un pago.
 *
 * Flujo normal: PENDIENTE → PAGADO.
 * REEMBOLSADO solo puede alcanzarse desde PAGADO (validado en {@link com.jorgelorza.clientes.pago.service.PagoService}).
 * FALLIDO queda disponible para integraciones externas de pasarela de pago que puedan reportar error.
 */
public enum EstadoPago {
    PENDIENTE,
    PAGADO,
    REEMBOLSADO,
    FALLIDO
}
