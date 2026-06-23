package com.jorgelorza.clientes.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de Spring Security.
 *
 * Decisiones de diseño:
 * - CSRF deshabilitado: la API es stateless (JWT), no usa cookies de sesión.
 * - Sesión STATELESS: no se crea ni usa HttpSession; cada request se autentica por JWT.
 * - {@code @EnableMethodSecurity}: habilita {@code @PreAuthorize} en controllers y services.
 * - {@link JwtAuthFilter} se registra antes de {@code UsernamePasswordAuthenticationFilter}
 *   para que la autenticación por token ocurra primero.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Define qué rutas son públicas y cuáles requieren autenticación.
     * El control fino por rol se hace con {@code @PreAuthorize} en cada endpoint.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Permite que el iframe de la consola H2 se cargue en dev
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .build();
    }

    /**
     * Conecta el {@link UserDetailsService} y el {@link PasswordEncoder} para que
     * Spring Security sepa cómo cargar y verificar las credenciales del usuario.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** Expuesto como bean para que {@link com.jorgelorza.clientes.auth.service.AuthService} pueda inyectarlo. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** BCrypt con coste por defecto (10 rondas) — seguro y estándar para contraseñas. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
