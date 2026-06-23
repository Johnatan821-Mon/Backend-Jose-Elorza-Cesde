package com.jorgelorza.clientes.auth.service;

import com.jorgelorza.clientes.auth.model.RefreshToken;
import com.jorgelorza.clientes.auth.repository.RefreshTokenRepository;
import com.jorgelorza.clientes.usuario.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /** Duración del refresh token en ms (por defecto 7 días). */
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * Crea un refresh token para el usuario, eliminando el anterior si existía.
     * Un solo token activo por usuario evita acumulación de tokens huérfanos.
     */
    @Transactional
    public RefreshToken create(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build();

        return refreshTokenRepository.save(token);
    }

    /**
     * Valida que el token exista en BD y no haya expirado.
     * Si expiró lo elimina y lanza excepción para que el cliente haga login de nuevo.
     *
     * @throws IllegalArgumentException si el token no existe o expiró.
     */
    @Transactional
    public RefreshToken validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token expirado — inicia sesión de nuevo");
        }

        return refreshToken;
    }

    /** Elimina el token del usuario — se llama en logout. */
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
