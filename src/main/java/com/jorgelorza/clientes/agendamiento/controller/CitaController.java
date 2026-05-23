package com.jorgelorza.clientes.agendamiento.controller;

import com.jorgelorza.clientes.agendamiento.dto.CitaRequest;
import com.jorgelorza.clientes.agendamiento.dto.CitaResponse;
import com.jorgelorza.clientes.agendamiento.model.EstadoCita;
import com.jorgelorza.clientes.agendamiento.service.CitaService;
import com.jorgelorza.clientes.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Endpoints REST para la gestión de citas.
 *
 * Endpoints de usuario autenticado (sin rol específico):
 * <ul>
 *   <li>POST   /api/citas              — crear cita propia</li>
 *   <li>GET    /api/citas/mis-citas    — listar citas propias</li>
 *   <li>DELETE /api/citas/{id}         — cancelar cita propia (validación en servicio)</li>
 * </ul>
 *
 * Endpoints exclusivos ADMIN:
 * <ul>
 *   <li>GET    /api/citas              — todas las citas</li>
 *   <li>GET    /api/citas/rango        — citas en rango de fechas (ISO-8601)</li>
 *   <li>PATCH  /api/citas/{id}/estado  — cambiar estado manualmente</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    /** POST /api/citas — el usuario autenticado agenda una cita para sí mismo. */
    @PostMapping
    public ResponseEntity<ApiResponse<CitaResponse>> crear(
            @Valid @RequestBody CitaRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(citaService.crear(auth.getName(), request)));
    }

    /** GET /api/citas/mis-citas — historial de citas del usuario autenticado. */
    @GetMapping("/mis-citas")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> misCitas(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(citaService.misCitas(auth.getName())));
    }

    /** GET /api/citas — todas las citas del sistema (ADMIN). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(citaService.listarTodas()));
    }

    /**
     * GET /api/citas/rango?inicio=&fin= — citas en un rango de fechas (ADMIN).
     * Las fechas se reciben en formato ISO-8601: {@code 2025-06-01T09:00:00}.
     */
    @GetMapping("/rango")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> porRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(ApiResponse.ok(citaService.listarPorRango(inicio, fin)));
    }

    /** PATCH /api/citas/{id}/estado?estado= — actualiza el estado de una cita (ADMIN). */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CitaResponse>> cambiarEstado(
            @PathVariable Long id, @RequestParam EstadoCita estado) {
        return ResponseEntity.ok(ApiResponse.ok(citaService.cambiarEstado(id, estado)));
    }

    /** DELETE /api/citas/{id} — cancela la cita; el servicio valida que pertenezca al usuario autenticado. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelar(
            @PathVariable Long id, Authentication auth) {
        citaService.cancelar(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Cita cancelada correctamente"));
    }
}
