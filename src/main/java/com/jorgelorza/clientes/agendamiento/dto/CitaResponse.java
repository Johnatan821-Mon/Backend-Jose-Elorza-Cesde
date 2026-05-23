package com.jorgelorza.clientes.agendamiento.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Proyección completa de una cita para la respuesta al cliente.
 *
 * Incluye datos desnormalizados del usuario y del servicio para evitar
 * que el cliente tenga que hacer múltiples llamadas. {@code precio} y
 * {@code duracionMinutos} se toman del servicio en el momento de la consulta.
 */
@Data
@Builder
public class CitaResponse {

    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;
    private Long servicioId;
    private String servicioNombre;
    private Integer duracionMinutos;
    private BigDecimal precio;
    private LocalDateTime fechaHora;
    private String estado;
    private String notas;
    private LocalDateTime fechaCreacion;
    /** Presente solo si la cita fue sincronizada con Google Calendar. */
    private String googleCalendarEventId;
}
