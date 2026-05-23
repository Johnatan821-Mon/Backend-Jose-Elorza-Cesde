package com.jorgelorza.clientes.servicio.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa un servicio ofrecido por el negocio (p.ej. corte, manicure).
 *
 * {@code activo} por defecto es {@code true}; desactivar un servicio no lo elimina
 * porque las citas ya agendadas mantienen la referencia. El precio se almacena con
 * precisión decimal (10,2) para evitar errores de redondeo en cálculos monetarios.
 */
@Entity
@Table(name = "servicios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    /** Duración en minutos; se usa para calcular la hora de fin al sincronizar con Google Calendar. */
    @Column(nullable = false)
    private Integer duracionMinutos;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /** Soft delete: {@code false} oculta el servicio en el catálogo público sin borrar el registro. */
    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;
}
