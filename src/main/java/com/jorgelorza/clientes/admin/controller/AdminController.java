package com.jorgelorza.clientes.admin.controller;

import com.jorgelorza.clientes.admin.dto.DashboardResponse;
import com.jorgelorza.clientes.admin.dto.ReporteIngresoResponse;
import com.jorgelorza.clientes.admin.service.AdminService;
import com.jorgelorza.clientes.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Endpoints exclusivos para administradores.
 *
 * {@code @PreAuthorize("hasRole('ADMIN')")} a nivel de clase protege todos los
 * métodos a la vez — cualquier otro rol recibe 403 antes de llegar al servicio.
 * No es necesario repetir la anotación en cada método salvo que uno requiera
 * una regla distinta.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /api/admin/dashboard
     *
     * Devuelve el snapshot actual del negocio: conteos de usuarios, citas por
     * estado e ingresos totales y del mes en curso.
     * No recibe parámetros — siempre refleja el momento de la llamada.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.dashboard()));
    }

    /**
     * GET /api/admin/reporte?inicio=&fin=
     *
     * Reporte de actividad e ingresos dentro de un rango de fechas.
     * {@code @DateTimeFormat(iso = DATE_TIME)} obliga al cliente a enviar las
     * fechas en formato ISO-8601, por ejemplo: {@code 2025-01-01T00:00:00}.
     *
     * @param inicio comienzo del período a analizar
     * @param fin    fin del período a analizar
     */
    @GetMapping("/reporte")
    public ResponseEntity<ApiResponse<ReporteIngresoResponse>> reporte(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.reporte(inicio, fin)));
    }
}
