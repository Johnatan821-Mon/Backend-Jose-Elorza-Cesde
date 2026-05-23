package com.jorgelorza.clientes.googlecalendar.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Proyección de un evento de Google Calendar devuelta al cliente.
 *
 * {@code eventId} es el id interno de Google Calendar; se almacena en
 * {@link com.jorgelorza.clientes.agendamiento.model.Cita#googleCalendarEventId}
 * al sincronizar para poder eliminarlo o actualizarlo después.
 * {@code enlace} es la URL directa al evento en Google Calendar.
 */
@Data
@Builder
public class EventoCalendarioResponse {

    private String eventId;
    private String titulo;
    private String descripcion;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private String enlace;
    private String estado;
}
