package com.kimngeam.backend.shared.security;

import com.kimngeam.backend.shared.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Generación y parseo de JWT. La clave secreta viene siempre de variable de
 * entorno ({@code JWT_SECRET}, ver application.yml) — nunca hardcodeada.
 */
@Component
public class JwtService {

	private static final String CLAIM_EMAIL = "email";
	private static final String CLAIM_NOMBRE = "nombre";
	private static final String CLAIM_ROL = "rol";

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String generarToken(Usuario usuario) {
		Instant ahora = Instant.now();
		return Jwts.builder()
				.id(UUID.randomUUID().toString())
				.subject(String.valueOf(usuario.getId()))
				.claim(CLAIM_EMAIL, usuario.getEmail())
				.claim(CLAIM_NOMBRE, usuario.getNombre())
				.claim(CLAIM_ROL, usuario.getRol().name().toLowerCase())
				.issuedAt(Date.from(ahora))
				.expiration(Date.from(ahora.plusMillis(expirationMs)))
				.signWith(key)
				.compact();
	}

	/**
	 * Parsea y valida la firma/expiración del token. Lanza {@link io.jsonwebtoken.JwtException}
	 * si el token es inválido, está mal formado o expiró.
	 */
	public Jws<Claims> parseToken(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
	}
}
