package com.jorgelorza.clientes.googlecalendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos de entrada para crear un evento manualmente en Google Calendar.
 *
 * Para sincronizar una cita existente (flujo más común) usar
 * {@code POST /api/google-calendar/citas/{id}/sincronizar} en lugar de este endpoint:
 * ese flujo construye el evento automáticamente a partir de los datos de la cita.
 */
@Data
public class EventoCalendarioRequest {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime inicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fin;

    /** Email del invitado que recibirá la invitación del calendario; opcional. */
    private String emailInvitado;
}
