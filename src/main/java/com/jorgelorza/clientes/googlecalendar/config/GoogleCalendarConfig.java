package com.jorgelorza.clientes.googlecalendar.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Configuración del cliente de Google Calendar API.
 *
 * Solo se carga cuando {@code google.calendar.enabled=true} (desactivado por defecto),
 * por lo que en dev y CI no se necesita el archivo {@code credentials.json}
 * ni acceso a internet hacia Google.
 *
 * Usa autenticación por Service Account (OAuth2): la cuenta de servicio debe tener
 * acceso delegado al calendario configurado en {@code google.calendar.calendar-id}.
 * El scope {@link CalendarScopes#CALENDAR} permite lectura y escritura de eventos.
 */
@Configuration
@ConditionalOnProperty(name = "google.calendar.enabled", havingValue = "true")
public class GoogleCalendarConfig {

    @Value("${google.calendar.credentials-file}")
    private String credentialsFile;

    @Value("${google.calendar.application-name}")
    private String applicationName;

    /**
     * Construye y expone el cliente de Google Calendar autenticado.
     * Lee el archivo JSON de credenciales de Service Account desde el path configurado.
     */
    @Bean
    public Calendar googleCalendar() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsFile))
                .createScoped(List.of(CalendarScopes.CALENDAR));

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(applicationName)
                .build();
    }
}
