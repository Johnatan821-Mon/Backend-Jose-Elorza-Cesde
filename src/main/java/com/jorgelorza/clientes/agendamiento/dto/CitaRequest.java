package com.jorgelorza.clientes.agendamiento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos de entrada para crear una cita.
 *
 * El usuario no se especifica en el body: se obtiene del JWT (email autenticado).
 * {@code @Future} garantiza a nivel de validación que no se agenden citas en el pasado.
 */
@Data
public class CitaRequest {

    @NotNull(message = "El servicio es obligatorio")
    private Long servicioId;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime fechaHora;

    private String notas;
}
