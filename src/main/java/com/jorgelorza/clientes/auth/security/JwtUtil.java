package com.jorgelorza.clientes.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilidad para generar y validar JSON Web Tokens.
 *
 * Usa HMAC-SHA256 con una clave derivada del secreto configurado en
 * {@code jwt.secret}. El token incluye como subject el email del usuario,
 * fecha de emisión y fecha de expiración.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /** Duración del token en milisegundos (por defecto 86400000 = 24 horas). */
    @Value("${jwt.expiration}")
    private long expiration;

    /** Genera un token sin claims adicionales. */
    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }

    /**
     * Genera un token con claims extra (p.ej. {@code Map.of("role", "ADMIN")}).
     * El subject es el username (email) del usuario.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /** Devuelve true si el token pertenece al usuario y no ha expirado. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /** Extrae el email (subject) del token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /** Extractor genérico: aplica una función sobre los Claims del token. */
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    /** Parsea y verifica la firma del token. Lanza excepción si la firma es inválida. */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Decodifica el secreto Base64 y construye la clave HMAC-SHA. */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
