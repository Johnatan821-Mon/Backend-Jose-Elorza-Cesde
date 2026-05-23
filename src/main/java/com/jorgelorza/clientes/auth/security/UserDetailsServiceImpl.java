package com.jorgelorza.clientes.auth.security;

import com.jorgelorza.clientes.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Puente entre Spring Security y la entidad {@link com.jorgelorza.clientes.auth.model.User}.
 *
 * Spring Security llama a {@code loadUserByUsername} durante la autenticación
 * (login y validación de JWT) para obtener el usuario y comparar credenciales.
 * Separarlo de {@link SecurityConfig} evita la dependencia circular que surgiría
 * si SecurityConfig inyectara JwtAuthFilter y este a su vez necesitara el UserDetailsService
 * definido en SecurityConfig.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /** El "username" en este sistema es el email del usuario. */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}
