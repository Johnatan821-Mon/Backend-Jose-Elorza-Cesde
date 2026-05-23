package com.jorgelorza.clientes.pago.repository;

import com.jorgelorza.clientes.pago.model.EstadoPago;
import com.jorgelorza.clientes.pago.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos para {@link Pago}.
 */
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /** Busca el pago de una cita específica; usado para validar duplicados antes de registrar. */
    Optional<Pago> findByCita_Id(Long citaId);

    List<Pago> findByEstado(EstadoPago estado);

    /** Historial de pagos de un usuario; navega la relación Pago→Cita→Usuario. */
    List<Pago> findByCita_Usuario_Id(Long usuarioId);

    /**
     * Suma de ingresos en un periodo: solo cuenta pagos en estado PAGADO.
     * {@code COALESCE(..., 0)} evita devolver {@code null} cuando no hay registros.
     * Usado por el módulo admin para el reporte de ingresos.
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estado = 'PAGADO' AND p.fechaPago BETWEEN :inicio AND :fin")
    BigDecimal sumIngresosPorPeriodo(LocalDateTime inicio, LocalDateTime fin);
}
