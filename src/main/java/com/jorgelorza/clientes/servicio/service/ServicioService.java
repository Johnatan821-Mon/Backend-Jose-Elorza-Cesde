package com.jorgelorza.clientes.servicio.service;

import com.jorgelorza.clientes.common.exception.ResourceNotFoundException;
import com.jorgelorza.clientes.servicio.dto.ServicioRequest;
import com.jorgelorza.clientes.servicio.dto.ServicioResponse;
import com.jorgelorza.clientes.servicio.model.Servicio;
import com.jorgelorza.clientes.servicio.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lógica de negocio para la gestión del catálogo de servicios.
 *
 * Dos variantes de listado: {@code listarActivos} para el catálogo público
 * y {@code listarTodos} para el panel admin (incluye desactivados).
 * {@code desactivar} aplica soft delete para preservar referencias en citas antiguas.
 * {@code findById} es público porque {@link com.jorgelorza.clientes.agendamiento.service.CitaService}
 * necesita cargar el servicio al crear una cita.
 */
@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;

    /** Solo servicios con {@code activo=true}; para el catálogo visible al cliente. */
    public List<ServicioResponse> listarActivos() {
        return servicioRepository.findByActivoTrue().stream().map(this::toResponse).toList();
    }

    /** Todos los servicios incluyendo desactivados; solo para ADMIN. */
    public List<ServicioResponse> listarTodos() {
        return servicioRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ServicioResponse obtener(Long id) {
        return toResponse(findById(id));
    }

    public ServicioResponse crear(ServicioRequest request) {
        Servicio servicio = Servicio.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .duracionMinutos(request.getDuracionMinutos())
                .precio(request.getPrecio())
                .build();
        return toResponse(servicioRepository.save(servicio));
    }

    public ServicioResponse actualizar(Long id, ServicioRequest request) {
        Servicio servicio = findById(id);
        servicio.setNombre(request.getNombre());
        servicio.setDescripcion(request.getDescripcion());
        servicio.setDuracionMinutos(request.getDuracionMinutos());
        servicio.setPrecio(request.getPrecio());
        return toResponse(servicioRepository.save(servicio));
    }

    /** Soft delete: pone {@code activo=false} sin eliminar el registro. */
    public void desactivar(Long id) {
        Servicio servicio = findById(id);
        servicio.setActivo(false);
        servicioRepository.save(servicio);
    }

    /**
     * Carga la entidad Servicio para uso interno y cross-módulo.
     *
     * @throws ResourceNotFoundException si el id no existe.
     */
    public Servicio findById(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + id));
    }

    private ServicioResponse toResponse(Servicio s) {
        return ServicioResponse.builder()
                .id(s.getId())
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .duracionMinutos(s.getDuracionMinutos())
                .precio(s.getPrecio())
                .activo(s.isActivo())
                .build();
    }
}
