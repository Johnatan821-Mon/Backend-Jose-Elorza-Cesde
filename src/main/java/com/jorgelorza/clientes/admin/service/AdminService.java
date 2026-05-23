package com.jorgelorza.clientes.admin.service;

import com.jorgelorza.clientes.admin.dto.DashboardResponse;
import com.jorgelorza.clientes.admin.dto.ReporteIngresoResponse;
import com.jorgelorza.clientes.agendamiento.model.EstadoCita;
import com.jorgelorza.clientes.agendamiento.repository.CitaRepository;
import com.jorgelorza.clientes.auth.repository.UserRepository;
import com.jorgelorza.clientes.pago.model.EstadoPago;
import com.jorgelorza.clientes.pago.repository.PagoRepository;
import com.jorgelorza.clientes.servicio.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lógica de negocio del módulo admin.
 *
 * No tiene repositorio propio: agrega datos de otros módulos (usuarios, citas,
 * pagos, servicios) para producir vistas consolidadas sin duplicar lógica.
 * Todas las operaciones son de solo lectura — ningún método modifica estado.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ServicioRepository servicioRepository;
    private final CitaRepository citaRepository;
    private final PagoRepository pagoRepository;

    /**
     * Construye el snapshot actual del negocio para el dashboard.
     *
     * Consulta cada repositorio de forma independiente y arma el DTO en una sola
     * pasada. {@code inicioMes} se recalcula en cada llamada para que el dato de
     * ingresos del mes siempre refleje el mes calendario en curso, sin necesidad
     * de un job o caché.
     *
     * @return {@link DashboardResponse} con conteos de usuarios, citas por estado
     *         e ingresos totales y del mes.
     */
    public DashboardResponse dashboard() {
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finMes = LocalDateTime.now();

        return DashboardResponse.builder()
                .totalUsuarios(userRepository.count())
                .totalServicios(servicioRepository.findByActivoTrue().size())
                .citasPendientes(citaRepository.findByEstadoOrderByFechaHora(EstadoCita.PENDIENTE).size())
                .citasConfirmadas(citaRepository.findByEstadoOrderByFechaHora(EstadoCita.CONFIRMADA).size())
                .citasCompletadas(citaRepository.findByEstadoOrderByFechaHora(EstadoCita.COMPLETADA).size())
                .citasCanceladas(citaRepository.findByEstadoOrderByFechaHora(EstadoCita.CANCELADA).size())
                .ingresosTotales(pagoRepository.sumIngresosPorPeriodo(
                        LocalDateTime.of(2000, 1, 1, 0, 0), finMes))
                .ingresosDelMes(pagoRepository.sumIngresosPorPeriodo(inicioMes, finMes))
                .pagosPendientes(pagoRepository.findByEstado(EstadoPago.PENDIENTE).size())
                .build();
    }

    /**
     * Genera un reporte de actividad e ingresos dentro de un rango de fechas.
     *
     * El cálculo de {@code servicioTop} agrupa todas las citas del período por
     * nombre de servicio, cuenta las ocurrencias de cada uno y selecciona el de
     * mayor frecuencia. Si no hay citas en el período devuelve "Sin datos".
     *
     * Los ingresos solo suman pagos en estado {@code PAGADO}; los reembolsados o
     * pendientes no se contabilizan (lógica delegada a {@code PagoRepository}).
     *
     * @param inicio fecha y hora de inicio del período (inclusive)
     * @param fin    fecha y hora de fin del período (inclusive)
     * @return {@link ReporteIngresoResponse} con totales, ingresos y servicio más solicitado
     */
    public ReporteIngresoResponse reporte(LocalDateTime inicio, LocalDateTime fin) {
        var citasEnPeriodo = citaRepository.findByFechaHoraBetweenOrderByFechaHora(inicio, fin);

        long completadas = citasEnPeriodo.stream()
                .filter(c -> c.getEstado() == EstadoCita.COMPLETADA).count();
        long canceladas = citasEnPeriodo.stream()
                .filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();

        // Agrupa por nombre de servicio y selecciona el más frecuente
        String servicioTop = citasEnPeriodo.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c.getServicio().getNombre(), java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("Sin datos");

        BigDecimal ingresos = pagoRepository.sumIngresosPorPeriodo(inicio, fin);

        return ReporteIngresoResponse.builder()
                .inicio(inicio)
                .fin(fin)
                .totalCitas(citasEnPeriodo.size())
                .citasCompletadas(completadas)
                .citasCanceladas(canceladas)
                .ingresos(ingresos)
                .servicioMasSolicitado(servicioTop)
                .build();
    }
}
