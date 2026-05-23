package com.jorgelorza.clientes.pago.model;

/**
 * Métodos de pago aceptados por el negocio.
 * Se almacena como String en BD para facilitar la adición de nuevos métodos sin migraciones.
 */
public enum MetodoPago {
    EFECTIVO,
    TARJETA_CREDITO,
    TARJETA_DEBITO,
    TRANSFERENCIA,
    OTRO
}
