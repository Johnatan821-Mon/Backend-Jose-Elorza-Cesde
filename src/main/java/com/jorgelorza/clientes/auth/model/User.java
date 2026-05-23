package com.jorgelorza.clientes.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entidad principal del sistema. Implementa {@link UserDetails} para que Spring
 * Security pueda usarla directamente en la cadena de autenticación sin una clase
 * adaptadora adicional.
 *
 * Los métodos isAccount*, isCredentials* e isEnabled devuelven {@code true} fijos;
 * la suspensión de cuentas se gestiona mediante el campo {@code active}, que
 * controla el acceso a nivel de negocio desde {@link com.jorgelorza.clientes.usuario.service.UsuarioService}.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Usado como username en Spring Security — debe ser único en la BD. */
    @Column(nullable = false, unique = true)
    private String email;

    /** Almacenado siempre con BCrypt — nunca en texto plano. */
    @Column(nullable = false)
    private String password;

    private String phone;

    /** Borrado lógico: false = cuenta desactivada por un admin. */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Convierte el rol en una autoridad reconocible por Spring Security. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /** Spring Security usa el email como identificador único del usuario. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
