package com.jorgelorza.clientes.redessociales.service;

import com.jorgelorza.clientes.common.exception.ResourceNotFoundException;
import com.jorgelorza.clientes.redessociales.dto.RedSocialRequest;
import com.jorgelorza.clientes.redessociales.dto.RedSocialResponse;
import com.jorgelorza.clientes.redessociales.model.RedSocial;
import com.jorgelorza.clientes.redessociales.repository.RedSocialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lógica de negocio para la gestión de perfiles de redes sociales del negocio.
 *
 * {@code listarActivas} para el listado público; {@code listarTodas} para el panel admin.
 * {@code eliminar} aplica soft delete: pone {@code activo=false} en lugar de borrar el registro.
 * {@code findById} es privado porque ningún otro módulo necesita cargar entidades de redes sociales.
 */
@Service
@RequiredArgsConstructor
public class RedSocialService {

    private final RedSocialRepository redSocialRepository;

    /** Solo redes activas; para el listado público visible al cliente. */
    public List<RedSocialResponse> listarActivas() {
        return redSocialRepository.findByActivoTrue().stream().map(this::toResponse).toList();
    }

    /** Todas las redes incluyendo desactivadas; para el panel admin. */
    public List<RedSocialResponse> listarTodas() {
        return redSocialRepository.findAll().stream().map(this::toResponse).toList();
    }

    public RedSocialResponse crear(RedSocialRequest request) {
        RedSocial red = RedSocial.builder()
                .tipo(request.getTipo())
                .nombre(request.getNombre())
                .url(request.getUrl())
                .build();
        return toResponse(redSocialRepository.save(red));
    }

    public RedSocialResponse actualizar(Long id, RedSocialRequest request) {
        RedSocial red = findById(id);
        red.setTipo(request.getTipo());
        red.setNombre(request.getNombre());
        red.setUrl(request.getUrl());
        return toResponse(redSocialRepository.save(red));
    }

    /** Soft delete: desactiva la red social sin eliminar el registro. */
    public void eliminar(Long id) {
        RedSocial red = findById(id);
        red.setActivo(false);
        redSocialRepository.save(red);
    }

    private RedSocial findById(Long id) {
        return redSocialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Red social no encontrada: " + id));
    }

    private RedSocialResponse toResponse(RedSocial r) {
        return RedSocialResponse.builder()
                .id(r.getId())
                .tipo(r.getTipo().name())
                .nombre(r.getNombre())
                .url(r.getUrl())
                .activo(r.isActivo())
                .build();
    }
}
