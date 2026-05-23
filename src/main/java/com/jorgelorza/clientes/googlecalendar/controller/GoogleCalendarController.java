package com.jorgelorza.clientes.googlecalendar.controller;

import com.jorgelorza.clientes.common.response.ApiResponse;
import com.jorgelorza.clientes.googlecalendar.dto.EventoCalendarioRequest;
import com.jorgelorza.clientes.googlecalendar.dto.EventoCalendarioResponse;
import com.jorgelorza.clientes.googlecalendar.service.GoogleCalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Endpoints REST para la integración con Google Calendar.
 *
 * Solo está disponible cuando {@code google.calendar.enabled=true}; en dev/CI
 * este controller no se registra y las rutas no existen, evitando errores de arranque
 * por ausencia del archivo de credenciales.
 *
 * Todos los endpoints requieren ROLE_ADMIN: la sincronización del calendario
 * es una operación interna del negocio, no accesible al cliente final.
 *
 * Endpoints:
 * <ul>
 *   <li>POST   /api/google-calendar/eventos               — crea evento manual</li>
 *   <li>POST   /api/google-calendar/citas/{id}/sincronizar — sincroniza cita existente</li>
 *   <li>DELETE /api/google-calendar/eventos/{eventId}      — elimina evento de Google</li>
 *   <li>GET    /api/google-calendar/eventos?inicio=&fin=   — lista eventos en rango (ISO-8601)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/google-calendar")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "google.calendar.enabled", havingValue = "true")
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;

    /** POST /api/google-calendar/eventos — crea un evento libre en el calendario (ADMIN). */
    @PostMapping("/eventos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventoCalendarioResponse>> crearEvento(
            @Valid @RequestBody EventoCalendarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(googleCalendarService.crearEvento(request)));
    }

    /**
     * POST /api/google-calendar/citas/{id}/sincronizar — crea el evento en Google Calendar
     * a partir de los datos de la cita y guarda el eventId en la cita (ADMIN).
     */
    @PostMapping("/citas/{citaId}/sincronizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventoCalendarioResponse>> sincronizarCita(@PathVariable Long citaId) {
        return ResponseEntity.ok(ApiResponse.ok(googleCalendarService.sincronizarCita(citaId)));
    }

    /** DELETE /api/google-calendar/eventos/{eventId} — elimina un evento de Google Calendar (ADMIN). */
    @DeleteMapping("/eventos/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminarEvento(@PathVariable String eventId) {
        googleCalendarService.eliminarEvento(eventId);
        return ResponseEntity.ok(ApiResponse.ok("Evento eliminado de Google Calendar"));
    }

    /** GET /api/google-calendar/eventos?inicio=&fin= — lista eventos en un rango de fechas (ADMIN). */
    @GetMapping("/eventos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EventoCalendarioResponse>>> listarEventos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(ApiResponse.ok(googleCalendarService.listarEventos(inicio, fin)));
    }
}
