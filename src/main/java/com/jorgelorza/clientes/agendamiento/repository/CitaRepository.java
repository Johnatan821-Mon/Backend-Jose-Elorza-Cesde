package com.jorgelorza.clientes.agendamiento.repository;

import com.jorgelorza.clientes.agendamiento.model.Cita;
import com.jorgelorza.clientes.agendamiento.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a datos para {@link Cita}.
 *
 * Los métodos derivados usan la convención de nombres de Spring Data JPA;
 * Hibernate genera el SQL correspondiente en tiempo de arranque.
 */
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /** Historial de citas de un usuario, ordenado de más reciente a más antigua. */
    List<Cita> findByUsuario_IdOrderByFechaHoraDesc(Long usuarioId);

    /** Citas filtradas por estado, ordenadas por fecha para la agenda del ADMIN. */
    List<Cita> findByEstadoOrderByFechaHora(EstadoCita estado);

    /** Citas dentro de un rango de fechas; usado para reportes y vista de agenda. */
    List<Cita> findByFechaHoraBetweenOrderByFechaHora(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Detecta solapamiento de horario: devuelve {@code true} si ya existe una cita
     * para el mismo servicio en la misma fecha/hora, excluyendo el estado indicado.
     * Se excluye CANCELADA para permitir reusar horarios liberados.
     */
    boolean existsByServicio_IdAndFechaHoraAndEstadoNot(Long servicioId, LocalDateTime fechaHora, EstadoCita estado);
}
