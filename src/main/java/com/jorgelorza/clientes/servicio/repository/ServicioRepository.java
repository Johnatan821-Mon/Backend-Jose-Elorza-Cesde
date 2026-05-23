package com.jorgelorza.clientes.servicio.repository;

import com.jorgelorza.clientes.servicio.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a datos para {@link Servicio}.
 *
 * {@code findByActivoTrue} implementa el filtro de catálogo público:
 * solo los servicios activos son visibles para clientes sin rol ADMIN.
 */
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByActivoTrue();
}
