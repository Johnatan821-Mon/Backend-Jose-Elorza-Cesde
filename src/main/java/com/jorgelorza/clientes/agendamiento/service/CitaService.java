package com.jorgelorza.clientes.agendamiento.service;

import com.jorgelorza.clientes.agendamiento.dto.CitaRequest;
import com.jorgelorza.clientes.agendamiento.dto.CitaResponse;
import com.jorgelorza.clientes.agendamiento.model.Cita;
import com.jorgelorza.clientes.agendamiento.model.EstadoCita;
import com.jorgelorza.clientes.agendamiento.repository.CitaRepository;
import com.jorgelorza.clientes.auth.model.User;
import com.jorgelorza.clientes.auth.repository.UserRepository;
import com.jorgelorza.clientes.common.exception.ResourceNotFoundException;
import com.jorgelorza.clientes.servicio.model.Servicio;
import com.jorgelorza.clientes.servicio.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio para la creación y gestión de citas.
 *
 * Reglas de negocio clave:
 * <ul>
 *   <li>No se pueden agendar dos citas para el mismo servicio en la misma fecha/hora
 *       (se excluyen las CANCELADAS del chequeo para reutilizar horarios).</li>
 *   <li>Solo el dueño de la cita puede cancelarla, y no puede cancelar una COMPLETADA.</li>
 *   <li>{@code findById} es público para que {@link com.jorgelorza.clientes.pago.service.PagoService}
 *       pueda cargar la cita al registrar un pago.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final UserRepository userRepository;
    private final ServicioService servicioService;

    /**
     * Crea una cita para el usuario autenticado.
     * Valida disponibilidad de horario antes de guardar.
     *
     * @param email email del usuario autenticado (extraído del JWT).
     * @throws IllegalArgumentException si el horario ya está ocupado.
     */
    public CitaResponse crear(String email, CitaRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Servicio servicio = servicioService.findById(request.getServicioId());

        if (citaRepository.existsByServicio_IdAndFechaHoraAndEstadoNot(
                request.getServicioId(), request.getFechaHora(), EstadoCita.CANCELADA)) {
            throw new IllegalArgumentException("El horario ya está ocupado para ese servicio");
        }

        Cita cita = Cita.builder()
                .usuario(user)
                .servicio(servicio)
                .fechaHora(request.getFechaHora())
                .notas(request.getNotas())
                .build();

        return toResponse(citaRepository.save(cita));
    }

    /** Devuelve las citas del usuario autenticado, ordenadas de más reciente a más antigua. */
    public List<CitaResponse> misCitas(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return citaRepository.findByUsuario_IdOrderByFechaHoraDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<CitaResponse> listarTodas() {
        return citaRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<CitaResponse> listarPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return citaRepository.findByFechaHoraBetweenOrderByFechaHora(inicio, fin)
                .stream().map(this::toResponse).toList();
    }

    /** Avanza o retrocede el estado de una cita. Solo el ADMIN puede invocar este método directamente. */
    public CitaResponse cambiarEstado(Long id, EstadoCita nuevoEstado) {
        Cita cita = findById(id);
        cita.setEstado(nuevoEstado);
        return toResponse(citaRepository.save(cita));
    }

    /**
     * Cancela una cita validando propiedad y estado.
     *
     * @throws IllegalArgumentException si la cita no pertenece al email indicado
     *                                  o si ya está COMPLETADA.
     */
    public void cancelar(Long citaId, String email) {
        Cita cita = findById(citaId);
        if (!cita.getUsuario().getEmail().equals(email)) {
            throw new IllegalArgumentException("No puedes cancelar una cita que no es tuya");
        }
        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new IllegalArgumentException("No se puede cancelar una cita completada");
        }
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    /**
     * Carga la entidad Cita para uso interno y cross-módulo (PagoService, GoogleCalendarService).
     *
     * @throws ResourceNotFoundException si el id no existe.
     */
    public Cita findById(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + id));
    }

    private CitaResponse toResponse(Cita c) {
        return CitaResponse.builder()
                .id(c.getId())
                .usuarioId(c.getUsuario().getId())
                .usuarioNombre(c.getUsuario().getName())
                .usuarioEmail(c.getUsuario().getEmail())
                .servicioId(c.getServicio().getId())
                .servicioNombre(c.getServicio().getNombre())
                .duracionMinutos(c.getServicio().getDuracionMinutos())
                .precio(c.getServicio().getPrecio())
                .fechaHora(c.getFechaHora())
                .estado(c.getEstado().name())
                .notas(c.getNotas())
                .fechaCreacion(c.getFechaCreacion())
                .googleCalendarEventId(c.getGoogleCalendarEventId())
                .build();
    }
}
