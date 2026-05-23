package com.jorgelorza.clientes.redessociales.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un perfil de red social del negocio.
 *
 * Permite gestionar los enlaces a redes sociales que se muestran en el sitio/app.
 * {@code activo} sigue el patrón soft delete: desactivar oculta la red del listado público
 * sin borrar el registro histórico.
 */
@Entity
@Table(name = "redes_sociales")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRedSocial tipo;

    /** Nombre descriptivo del perfil (p.ej. "@mi_negocio"). */
    @Column(nullable = false)
    private String nombre;

    /** URL completa del perfil en la plataforma. */
    @Column(nullable = false)
    private String url;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;
}
