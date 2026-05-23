package com.jorgelorza.clientes.auth.repository;

import com.jorgelorza.clientes.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de usuarios. Spring Data JPA genera la implementación en tiempo de arranque
 * a partir de los nombres de los métodos, sin necesidad de escribir SQL.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Usado en el login y en la carga de seguridad para localizar al usuario por email. */
    Optional<User> findByEmail(String email);

    /** Evita registrar dos usuarios con el mismo email antes de intentar el INSERT. */
    boolean existsByEmail(String email);
}
