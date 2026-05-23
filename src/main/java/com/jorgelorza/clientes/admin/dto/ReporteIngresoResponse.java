package com.jorgelorza.clientes.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Resultado del reporte por rango de fechas. Lo devuelve GET /api/admin/reporte. */
@Data
@Builder
public class ReporteIngresoResponse {

    private LocalDateTime inicio;
    private LocalDateTime fin;
    private long totalCitas;             // Todas las citas dentro del rango, sin importar estado
    private long citasCompletadas;
    private long citasCanceladas;
    private BigDecimal ingresos;         // Solo pagos en estado PAGADO dentro del rango
    private String servicioMasSolicitado; // Servicio con más citas en el período
}
