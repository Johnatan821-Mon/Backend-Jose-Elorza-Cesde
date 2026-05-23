package com.jorgelorza.clientes.agendamiento.model;

/**
 * Ciclo de vida de una cita.
 *
 * El flujo normal es: PENDIENTE → CONFIRMADA → EN_CURSO → COMPLETADA.
 * CANCELADA es un estado terminal: no se puede avanzar desde él
 * ni registrar un pago sobre una cita cancelada.
 * Los estados se persisten como String en BD (no como ordinal)
 * para que añadir valores futuros no rompa datos existentes.
 */
public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    EN_CURSO,
    COMPLETADA,
    CANCELADA
}
