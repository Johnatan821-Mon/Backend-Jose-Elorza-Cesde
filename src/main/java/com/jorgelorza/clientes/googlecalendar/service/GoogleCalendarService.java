package com.jorgelorza.clientes.googlecalendar.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.jorgelorza.clientes.agendamiento.model.Cita;
import com.jorgelorza.clientes.agendamiento.repository.CitaRepository;
import com.jorgelorza.clientes.common.exception.ResourceNotFoundException;
import com.jorgelorza.clientes.googlecalendar.dto.EventoCalendarioRequest;
import com.jorgelorza.clientes.googlecalendar.dto.EventoCalendarioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Servicio de integración con Google Calendar API.
 *
 * Solo se instancia cuando {@code google.calendar.enabled=true}; en otro caso Spring
 * no registra este bean y los endpoints del módulo tampoco están disponibles.
 *
 * Flujo de sincronización de cita ({@code sincronizarCita}):
 * 1. Carga la cita con su servicio y usuario asociados.
 * 2. Calcula la hora de fin sumando {@code duracionMinutos} del servicio.
 * 3. Crea el evento en Google Calendar con el usuario como invitado.
 * 4. Guarda el {@code eventId} devuelto por Google en la cita para referencia futura.
 *
 * {@code setSingleEvents(true)} en {@code listarEventos} expande eventos recurrentes
 * para que la API devuelva ocurrencias individuales en lugar de la regla de recurrencia.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "google.calendar.enabled", havingValue = "true")
public class GoogleCalendarService {

    private final Calendar googleCalendar;
    private final CitaRepository citaRepository;

    @Value("${google.calendar.calendar-id}")
    private String calendarId;

    @Value("${google.calendar.timezone:America/Bogota}")
    private String timezone;

    /**
     * Crea un evento en Google Calendar con los datos del request.
     * Para sincronizar una cita existente usar {@code sincronizarCita} en su lugar.
     */
    public EventoCalendarioResponse crearEvento(EventoCalendarioRequest request) {
        try {
            Event event = buildEvent(request.getTitulo(), request.getDescripcion(),
                    request.getInicio(), request.getFin(), request.getEmailInvitado());

            Event created = googleCalendar.events().insert(calendarId, event).execute();
            return toResponse(created);
        } catch (IOException e) {
            log.error("Error al crear evento en Google Calendar", e);
            throw new RuntimeException("No se pudo crear el evento en Google Calendar");
        }
    }

    /**
     * Sincroniza una cita con Google Calendar: crea el evento y persiste el eventId en la cita.
     * Invitar al usuario como attendee dispara una notificación de Google Calendar a su email.
     */
    public EventoCalendarioResponse sincronizarCita(Long citaId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + citaId));

        String titulo = cita.getServicio().getNombre() + " - " + cita.getUsuario().getName();
        LocalDateTime fin = cita.getFechaHora().plusMinutes(cita.getServicio().getDuracionMinutos());

        try {
            Event event = buildEvent(titulo, cita.getNotas(), cita.getFechaHora(), fin,
                    cita.getUsuario().getEmail());
            Event created = googleCalendar.events().insert(calendarId, event).execute();

            cita.setGoogleCalendarEventId(created.getId());
            citaRepository.save(cita);

            return toResponse(created);
        } catch (IOException e) {
            log.error("Error al sincronizar cita {} con Google Calendar", citaId, e);
            throw new RuntimeException("No se pudo sincronizar la cita con Google Calendar");
        }
    }

    public void eliminarEvento(String eventId) {
        try {
            googleCalendar.events().delete(calendarId, eventId).execute();
        } catch (IOException e) {
            log.error("Error al eliminar evento {} de Google Calendar", eventId, e);
            throw new RuntimeException("No se pudo eliminar el evento de Google Calendar");
        }
    }

    /** Lista eventos en un rango de fechas. {@code setSingleEvents(true)} expande recurrencias. */
    public List<EventoCalendarioResponse> listarEventos(LocalDateTime inicio, LocalDateTime fin) {
        try {
            DateTime timeMin = new DateTime(toDate(inicio));
            DateTime timeMax = new DateTime(toDate(fin));

            return googleCalendar.events().list(calendarId)
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
                    .getItems()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } catch (IOException e) {
            log.error("Error al listar eventos de Google Calendar", e);
            throw new RuntimeException("No se pudo obtener los eventos de Google Calendar");
        }
    }

    /** Construye el objeto Event de la API de Google con todos sus campos. */
    private Event buildEvent(String titulo, String descripcion,
                             LocalDateTime inicio, LocalDateTime fin, String emailInvitado) {
        Event event = new Event()
                .setSummary(titulo)
                .setDescription(descripcion)
                .setStart(toEventDateTime(inicio))
                .setEnd(toEventDateTime(fin));

        if (emailInvitado != null && !emailInvitado.isBlank()) {
            event.setAttendees(List.of(new EventAttendee().setEmail(emailInvitado)));
        }
        return event;
    }

    /** Convierte un LocalDateTime a {@link EventDateTime} con la zona horaria configurada. */
    private EventDateTime toEventDateTime(LocalDateTime ldt) {
        return new EventDateTime()
                .setDateTime(new DateTime(toDate(ldt)))
                .setTimeZone(timezone);
    }

    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.of(timezone)).toInstant());
    }

    private EventoCalendarioResponse toResponse(Event event) {
        return EventoCalendarioResponse.builder()
                .eventId(event.getId())
                .titulo(event.getSummary())
                .descripcion(event.getDescription())
                .enlace(event.getHtmlLink())
                .estado(event.getStatus())
                .build();
    }
}
