package com.jorgelorza.clientes.pago.service;

import com.jorgelorza.clientes.agendamiento.model.Cita;
import com.jorgelorza.clientes.agendamiento.model.EstadoCita;
import com.jorgelorza.clientes.agendamiento.service.CitaService;
import com.jorgelorza.clientes.common.exception.ResourceNotFoundException;
import com.jorgelorza.clientes.pago.dto.PagoRequest;
import com.jorgelorza.clientes.pago.dto.PagoResponse;
import com.jorgelorza.clientes.pago.model.EstadoPago;
import com.jorgelorza.clientes.pago.model.Pago;
import com.jorgelorza.clientes.pago.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio para el registro y gestión de pagos.
 *
 * Reglas clave:
 * <ul>
 *   <li>No se puede registrar un pago si la cita ya tiene uno ({@code findByCita_Id} no vacío).</li>
 *   <li>No se pueden cobrar citas canceladas.</li>
 *   <li>Al registrar un pago exitoso, la cita pasa automáticamente a COMPLETADA.</li>
 *   <li>Solo se puede reembolsar un pago que esté en estado PAGADO.</li>
 * </ul>
 *
 * El monto se toma de {@code cita.getServicio().getPrecio()} y no del request
 * para evitar que el ADMIN ingrese un monto incorrecto por error.
 */
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final CitaService citaService;

    /**
     * Registra el pago de una cita y la marca como COMPLETADA.
     *
     * @throws IllegalArgumentException si la cita ya tiene pago o está cancelada.
     */
    public PagoResponse registrar(PagoRequest request) {
        Cita cita = citaService.findById(request.getCitaId());

        if (pagoRepository.findByCita_Id(cita.getId()).isPresent()) {
            throw new IllegalArgumentException("Esta cita ya tiene un pago registrado");
        }
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException("No se puede registrar el pago de una cita cancelada");
        }

        Pago pago = Pago.builder()
                .cita(cita)
                .monto(cita.getServicio().getPrecio()) // monto fijo del servicio, no editable por el cliente
                .estado(EstadoPago.PAGADO)
                .metodoPago(request.getMetodoPago())
                .referencia(request.getReferencia())
                .fechaPago(LocalDateTime.now())
                .build();

        citaService.cambiarEstado(cita.getId(), EstadoCita.COMPLETADA);
        return toResponse(pagoRepository.save(pago));
    }

    public PagoResponse obtenerPorCita(Long citaId) {
        Pago pago = pagoRepository.findByCita_Id(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado para la cita: " + citaId));
        return toResponse(pago);
    }

    public List<PagoResponse> listarTodos() {
        return pagoRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<PagoResponse> listarPorUsuario(Long usuarioId) {
        return pagoRepository.findByCita_Usuario_Id(usuarioId).stream().map(this::toResponse).toList();
    }

    /**
     * Marca un pago como REEMBOLSADO. Solo aplica a pagos en estado PAGADO.
     *
     * @throws IllegalArgumentException si el pago no está en estado PAGADO.
     */
    public PagoResponse reembolsar(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + id));
        if (pago.getEstado() != EstadoPago.PAGADO) {
            throw new IllegalArgumentException("Solo se pueden reembolsar pagos en estado PAGADO");
        }
        pago.setEstado(EstadoPago.REEMBOLSADO);
        return toResponse(pagoRepository.save(pago));
    }

    private PagoResponse toResponse(Pago p) {
        return PagoResponse.builder()
                .id(p.getId())
                .citaId(p.getCita().getId())
                .usuarioNombre(p.getCita().getUsuario().getName())
                .servicioNombre(p.getCita().getServicio().getNombre())
                .monto(p.getMonto())
                .estado(p.getEstado().name())
                .metodoPago(p.getMetodoPago() != null ? p.getMetodoPago().name() : null)
                .referencia(p.getReferencia())
                .fechaPago(p.getFechaPago())
                .fechaCreacion(p.getFechaCreacion())
                .build();
    }
}
