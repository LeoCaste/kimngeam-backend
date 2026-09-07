package com.kimngeam.backend.shared.security;

import com.kimngeam.backend.shared.entity.Rol;
import com.kimngeam.backend.shared.entity.TokenInvalidadoRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Valida el JWT en cada request y puebla el {@link SecurityContextHolder}.
 * Nunca lanza excepción: si el token falta, es inválido o fue invalidado en
 * logout, simplemente deja la request sin autenticar — es la configuración de
 * autorización en {@link SecurityConfig} la que decide si eso basta 401/403.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;
	private final TokenInvalidadoRepository tokenInvalidadoRepository;

	public JwtAuthenticationFilter(JwtService jwtService, TokenInvalidadoRepository tokenInvalidadoRepository) {
		this.jwtService = jwtService;
		this.tokenInvalidadoRepository = tokenInvalidadoRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		extraerToken(request).ifPresent(token -> autenticarSiValido(token));
		filterChain.doFilter(request, response);
	}

	private Optional<String> extraerToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(BEARER_PREFIX)) {
			return Optional.of(header.substring(BEARER_PREFIX.length()));
		}
		return Optional.empty();
	}

	private void autenticarSiValido(String token) {
		try {
			Claims claims = jwtService.parseToken(token).getPayload();
			if (tokenInvalidadoRepository.existsById(claims.getId())) {
				return;
			}
			SecurityContextHolder.getContext().setAuthentication(construirAuthentication(claims));
		} catch (JwtException | IllegalArgumentException ex) {
			SecurityContextHolder.clearContext();
		}
	}

	private UsernamePasswordAuthenticationToken construirAuthentication(Claims claims) {
		Rol rol = Rol.valueOf(claims.get("rol", String.class).toUpperCase());
		AuthenticatedUsuario principal = new AuthenticatedUsuario(
				Long.valueOf(claims.getSubject()),
				claims.get("email", String.class),
				claims.get("nombre", String.class),
				rol);
		List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
		return UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);
	}
}
