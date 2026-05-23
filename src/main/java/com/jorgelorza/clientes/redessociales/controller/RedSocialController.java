package com.jorgelorza.clientes.redessociales.controller;

import com.jorgelorza.clientes.common.response.ApiResponse;
import com.jorgelorza.clientes.redessociales.dto.RedSocialRequest;
import com.jorgelorza.clientes.redessociales.dto.RedSocialResponse;
import com.jorgelorza.clientes.redessociales.service.RedSocialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST para la gestión de redes sociales del negocio.
 *
 * GET público devuelve solo redes activas (para mostrar en el sitio/app).
 * GET /todas, POST, PUT y DELETE son exclusivos de ADMIN.
 * DELETE aplica soft delete: la red queda inactiva pero no se elimina.
 */
@RestController
@RequestMapping("/api/redes-sociales")
@RequiredArgsConstructor
public class RedSocialController {

    private final RedSocialService redSocialService;

    /** GET /api/redes-sociales — redes activas visibles públicamente. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RedSocialResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(redSocialService.listarActivas()));
    }

    /** GET /api/redes-sociales/todas — todas las redes incluyendo desactivadas (ADMIN). */
    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RedSocialResponse>>> listarTodas() {
        return ResponseEntity.ok(ApiResponse.ok(redSocialService.listarTodas()));
    }

    /** POST /api/redes-sociales — crea una nueva red social, retorna 201 Created (ADMIN). */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RedSocialResponse>> crear(@Valid @RequestBody RedSocialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(redSocialService.crear(request)));
    }

    /** PUT /api/redes-sociales/{id} — actualiza todos los campos de la red (ADMIN). */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RedSocialResponse>> actualizar(
            @PathVariable Long id, @Valid @RequestBody RedSocialRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(redSocialService.actualizar(id, request)));
    }

    /** DELETE /api/redes-sociales/{id} — desactiva la red social (soft delete, ADMIN). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        redSocialService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Red social desactivada correctamente"));
    }
}
