package com.kimngeam.backend.shared.security;

import com.kimngeam.backend.shared.entity.TokenInvalidadoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Roles del dominio: {@code academico} y {@code admin} (ver {@link com.kimngeam.backend.shared.entity.Rol}).
 * Sesión stateless: la única fuente de identidad es el JWT, nunca una sesión
 * de servidor. CSRF deshabilitado por ser una API sin cookies de sesión.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtService jwtService;
	private final TokenInvalidadoRepository tokenInvalidadoRepository;
	private final RestAuthenticationEntryPoint authenticationEntryPoint;
	private final RestAccessDeniedHandler accessDeniedHandler;

	public SecurityConfig(JwtService jwtService, TokenInvalidadoRepository tokenInvalidadoRepository,
			RestAuthenticationEntryPoint authenticationEntryPoint, RestAccessDeniedHandler accessDeniedHandler) {
		this.jwtService = jwtService;
		this.tokenInvalidadoRepository = tokenInvalidadoRepository;
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.accessDeniedHandler = accessDeniedHandler;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.POST, "/auth/login", "/auth/login-admin", "/auth/registro")
						.permitAll()
						.requestMatchers(HttpMethod.POST, "/traductor/traducir").permitAll()
						.requestMatchers("/validaciones/**").hasRole("ACADEMICO")
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.addFilterBefore(new JwtAuthenticationFilter(jwtService, tokenInvalidadoRepository),
						UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
