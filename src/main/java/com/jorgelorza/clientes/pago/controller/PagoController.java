package com.jorgelorza.clientes.pago.controller;

import com.jorgelorza.clientes.common.response.ApiResponse;
import com.jorgelorza.clientes.pago.dto.PagoRequest;
import com.jorgelorza.clientes.pago.dto.PagoResponse;
import com.jorgelorza.clientes.pago.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST para la gestión de pagos.
 *
 * La mayoría de operaciones son exclusivas de ADMIN.
 * La excepción es GET /api/pagos/cita/{citaId}, que cualquier usuario autenticado
 * puede consultar para saber si su cita ya fue pagada.
 *
 * POST registra el pago y automáticamente cambia el estado de la cita a COMPLETADA.
 * PATCH /{id}/reembolsar solo aplica a pagos en estado PAGADO.
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    /** POST /api/pagos — registra un pago y completa la cita asociada (ADMIN). */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagoResponse>> registrar(@Valid @RequestBody PagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(pagoService.registrar(request)));
    }

    /** GET /api/pagos — lista todos los pagos del sistema (ADMIN). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(pagoService.listarTodos()));
    }

    /** GET /api/pagos/cita/{citaId} — consulta el pago de una cita (cualquier usuario autenticado). */
    @GetMapping("/cita/{citaId}")
    public ResponseEntity<ApiResponse<PagoResponse>> porCita(@PathVariable Long citaId) {
        return ResponseEntity.ok(ApiResponse.ok(pagoService.obtenerPorCita(citaId)));
    }

    /** GET /api/pagos/usuario/{usuarioId} — historial de pagos de un usuario (ADMIN). */
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> porUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(ApiResponse.ok(pagoService.listarPorUsuario(usuarioId)));
    }

    /** PATCH /api/pagos/{id}/reembolsar — marca el pago como REEMBOLSADO (ADMIN). */
    @PatchMapping("/{id}/reembolsar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagoResponse>> reembolsar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(pagoService.reembolsar(id)));
    }
}
