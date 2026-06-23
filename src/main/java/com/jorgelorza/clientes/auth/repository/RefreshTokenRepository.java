package com.jorgelorza.clientes.auth.repository;

import com.jorgelorza.clientes.auth.model.RefreshToken;
import com.jorgelorza.clientes.usuario.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /** Elimina el token activo del usuario — se llama antes de crear uno nuevo y en logout. */
    @Modifying
    void deleteByUser(User user);
}
