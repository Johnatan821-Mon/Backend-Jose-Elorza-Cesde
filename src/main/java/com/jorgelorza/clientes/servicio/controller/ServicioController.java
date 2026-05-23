package com.jorgelorza.clientes.servicio.controller;

import com.jorgelorza.clientes.common.response.ApiResponse;
import com.jorgelorza.clientes.servicio.dto.ServicioRequest;
import com.jorgelorza.clientes.servicio.dto.ServicioResponse;
import com.jorgelorza.clientes.servicio.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST para el catálogo de servicios.
 *
 * Lectura pública (GET sin ADMIN): cualquier usuario autenticado puede ver servicios activos.
 * Escritura restringida a ADMIN: crear, actualizar y desactivar requieren ROLE_ADMIN.
 * DELETE es un soft delete — el servicio queda inactivo pero no se borra de la BD.
 */
@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    /** GET /api/servicios — catálogo público de servicios activos. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(servicioService.listarActivos()));
    }

    /** GET /api/servicios/todos — todos los servicios incluyendo desactivados (ADMIN). */
    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok(servicioService.listarTodos()));
    }

    /** GET /api/servicios/{id} — detalle de un servicio. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(servicioService.obtener(id)));
    }

    /** POST /api/servicios — crea un servicio nuevo, retorna 201 Created (ADMIN). */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServicioResponse>> crear(@Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(servicioService.crear(request)));
    }

    /** PUT /api/servicios/{id} — reemplaza todos los campos del servicio (ADMIN). */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServicioResponse>> actualizar(
            @PathVariable Long id, @Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(servicioService.actualizar(id, request)));
    }

    /** DELETE /api/servicios/{id} — desactiva el servicio (soft delete, ADMIN). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long id) {
        servicioService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.ok("Servicio desactivado correctamente"));
    }
}
