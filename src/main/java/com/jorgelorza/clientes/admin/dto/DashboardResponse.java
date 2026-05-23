package com.jorgelorza.clientes.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** Snapshot del estado actual del negocio. Lo devuelve GET /api/admin/dashboard. */
@Data
@Builder
public class DashboardResponse {

    private long totalUsuarios;
    private long totalServicios;       // Solo servicios activos
    private long citasPendientes;
    private long citasConfirmadas;
    private long citasCompletadas;
    private long citasCanceladas;
    private BigDecimal ingresosTotales; // Suma histórica de todos los pagos en estado PAGADO
    private BigDecimal ingresosDelMes;  // Suma de pagos PAGADO desde el día 1 del mes actual
    private long pagosPendientes;
}
