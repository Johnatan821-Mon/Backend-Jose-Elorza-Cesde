package com.jorgelorza.clientes.agendamiento.model;

import com.jorgelorza.clientes.auth.model.User;
import com.jorgelorza.clientes.servicio.model.Servicio;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad central del módulo de agendamiento: representa una reserva de un servicio por un usuario.
 *
 * Relaciones LAZY: {@code usuario} y {@code servicio} se cargan solo cuando se acceden,
 * evitando consultas innecesarias. La proyección {@link com.jorgelorza.clientes.agendamiento.dto.CitaResponse}
 * accede a ambas relaciones, por lo que siempre se resuelven dentro de la misma transacción.
 *
 * {@code googleCalendarEventId} almacena el id del evento creado en Google Calendar
 * al sincronizar; permite actualizarlo o eliminarlo desde {@link com.jorgelorza.clientes.googlecalendar.service.GoogleCalendarService}.
 */
@Entity
@Table(name = "citas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    /** Estado inicial PENDIENTE; el flujo lo avanza el ADMIN o el propio cancelado del cliente. */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    @Column(length = 500)
    private String notas;

    /** Timestamp de creación gestionado por Hibernate; no editable después de la inserción. */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Id del evento en Google Calendar; {@code null} si la cita no ha sido sincronizada. */
    private String googleCalendarEventId;
}
