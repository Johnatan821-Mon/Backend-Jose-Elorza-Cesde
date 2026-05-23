package com.jorgelorza.clientes.pago.model;

import com.jorgelorza.clientes.agendamiento.model.Cita;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa el pago asociado a una cita.
 *
 * La relación con {@link Cita} es OneToOne con {@code unique=true} en {@code cita_id}:
 * cada cita puede tener como máximo un pago registrado.
 * El monto se toma del precio del servicio en el momento del registro, no del request,
 * para garantizar que coincida con lo acordado al agendar.
 *
 * {@code fechaPago} es la fecha real del cobro (registrada en el servicio);
 * {@code fechaCreacion} es el timestamp de inserción del registro en BD.
 */
@Entity
@Table(name = "pagos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Restricción única en BD garantiza un solo pago por cita. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Cita cita;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    /** Número de referencia de la transacción (ticket, comprobante, etc.). Opcional. */
    private String referencia;

    /** Momento en que se efectuó el cobro; null hasta que el pago pase a PAGADO. */
    private LocalDateTime fechaPago;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}
