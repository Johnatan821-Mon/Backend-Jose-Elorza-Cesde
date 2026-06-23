package com.jorgelorza.clientes.auth.model;

import com.jorgelorza.clientes.usuario.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Token de refresco almacenado en BD para poder invalidarlo en logout.
 * Un usuario tiene como máximo un refresh token activo: al crear uno nuevo
 * se elimina el anterior del mismo usuario.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** UUID aleatorio generado en {@link com.jorgelorza.clientes.auth.service.RefreshTokenService}. */
    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
