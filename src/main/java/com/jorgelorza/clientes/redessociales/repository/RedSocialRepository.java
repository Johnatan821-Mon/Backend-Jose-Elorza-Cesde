package com.jorgelorza.clientes.redessociales.repository;

import com.jorgelorza.clientes.redessociales.model.RedSocial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a datos para {@link RedSocial}.
 * {@code findByActivoTrue} filtra el catálogo público para mostrar solo redes activas.
 */
public interface RedSocialRepository extends JpaRepository<RedSocial, Long> {

    List<RedSocial> findByActivoTrue();
}
