package com.jorgelorza.clientes.usuario.controller;

import com.jorgelorza.clientes.common.response.ApiResponse;
import com.jorgelorza.clientes.usuario.dto.ActualizarPerfilRequest;
import com.jorgelorza.clientes.usuario.dto.UsuarioResponse;
import com.jorgelorza.clientes.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST para la gestión de usuarios.
 *
 * Los endpoints de perfil (/perfil) están disponibles para cualquier usuario autenticado;
 * los de administración (listar, obtener por id, desactivar) requieren ROLE_ADMIN.
 *
 * El email del usuario autenticado se obtiene de {@link Authentication#getName()},
 * que devuelve el subject del JWT — en este sistema, el email.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /** GET /api/usuarios — lista todos los usuarios (ADMIN). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.listarTodos()));
    }

    /** GET /api/usuarios/perfil — devuelve el perfil del usuario autenticado. */
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioResponse>> perfil(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.obtenerPerfil(auth.getName())));
    }

    /** PUT /api/usuarios/perfil — actualiza nombre y teléfono del usuario autenticado. */
    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(
            @Valid @RequestBody ActualizarPerfilRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.actualizarPerfil(auth.getName(), request)));
    }

    /** GET /api/usuarios/{id} — obtiene un usuario por id (ADMIN). */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.obtenerPorId(id)));
    }

    /** DELETE /api/usuarios/{id} — desactiva el usuario (soft delete, ADMIN). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario desactivado correctamente"));
    }
}
