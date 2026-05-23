package com.jorgelorza.clientes.usuario.service;

import com.jorgelorza.clientes.auth.model.User;
import com.jorgelorza.clientes.auth.repository.UserRepository;
import com.jorgelorza.clientes.common.exception.ResourceNotFoundException;
import com.jorgelorza.clientes.usuario.dto.ActualizarPerfilRequest;
import com.jorgelorza.clientes.usuario.dto.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lógica de negocio para la gestión de usuarios.
 *
 * Reutiliza {@link UserRepository} del módulo auth en lugar de definir
 * una entidad propia: el usuario ya existe, este módulo solo expone
 * operaciones de lectura y edición de perfil sobre él.
 *
 * La desactivación es un soft delete: el campo {@code active} pasa a {@code false}
 * pero el registro permanece en BD para preservar la integridad referencial con citas y pagos.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UserRepository userRepository;

    /** Devuelve todos los usuarios (activos e inactivos). Solo para ADMIN. */
    public List<UsuarioResponse> listarTodos() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UsuarioResponse obtenerPorId(Long id) {
        return toResponse(findById(id));
    }

    /** Carga el perfil del usuario autenticado a partir de su email extraído del JWT. */
    public UsuarioResponse obtenerPerfil(String email) {
        return toResponse(findByEmail(email));
    }

    /** Actualiza nombre y teléfono del usuario autenticado. El email y el rol no son editables aquí. */
    public UsuarioResponse actualizarPerfil(String email, ActualizarPerfilRequest request) {
        User user = findByEmail(email);
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        return toResponse(userRepository.save(user));
    }

    /** Soft delete: desactiva el usuario sin eliminarlo de la BD. */
    public void desactivar(Long id) {
        User user = findById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Exposición pública para consultas cruzadas desde otros módulos (p.ej. CitaService).
     *
     * @throws ResourceNotFoundException si el id no existe.
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
    }

    private UsuarioResponse toResponse(User user) {
        return UsuarioResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .active(user.isActive())
                .build();
    }
}
