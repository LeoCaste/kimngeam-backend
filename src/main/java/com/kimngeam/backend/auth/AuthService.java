package com.kimngeam.backend.auth;

import com.kimngeam.backend.auth.dto.AuthResponse;
import com.kimngeam.backend.auth.dto.LoginRequest;
import com.kimngeam.backend.auth.dto.RegistroRequest;
import com.kimngeam.backend.auth.dto.UsuarioResponse;
import com.kimngeam.backend.shared.entity.EstadoUsuario;
import com.kimngeam.backend.shared.entity.Rol;
import com.kimngeam.backend.shared.entity.TokenInvalidado;
import com.kimngeam.backend.shared.entity.TokenInvalidadoRepository;
import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.UsuarioRepository;
import com.kimngeam.backend.shared.error.ConflictException;
import com.kimngeam.backend.shared.error.UnauthorizedException;
import com.kimngeam.backend.shared.security.JwtService;
import io.jsonwebtoken.Claims;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * La invalidación de tokens (logout) se resuelve por {@code jti}, no por
 * sesión de servidor: la única fuente de verdad de identidad es el JWT.
 */
@Service
public class AuthService {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String CREDENCIALES_INVALIDAS = "Credenciales incorrectas";
	private static final String TOKEN_INVALIDO = "Token inválido o expirado";

	private final UsuarioRepository usuarioRepository;
	private final TokenInvalidadoRepository tokenInvalidadoRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UsuarioRepository usuarioRepository, TokenInvalidadoRepository tokenInvalidadoRepository,
			PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.usuarioRepository = usuarioRepository;
		this.tokenInvalidadoRepository = tokenInvalidadoRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public AuthResponse login(LoginRequest request) {
		Usuario usuario = autenticar(request.email(), request.password());
		return construirRespuestaConAcceso(usuario);
	}

	/**
	 * No distingue en el mensaje si las credenciales fallaron o si el usuario
	 * existe pero no es admin — ambos casos devuelven el mismo 401 genérico.
	 */
	public AuthResponse loginAdmin(LoginRequest request) {
		Usuario usuario = autenticar(request.email(), request.password());
		if (usuario.getRol() != Rol.ADMIN) {
			throw new UnauthorizedException(CREDENCIALES_INVALIDAS);
		}
		return construirRespuestaConAcceso(usuario);
	}

	public AuthResponse registrar(RegistroRequest request) {
		if (usuarioRepository.existsByEmail(request.email())) {
			throw new ConflictException("EMAIL_TAKEN", "El correo ya está registrado");
		}
		Usuario usuario = new Usuario(request.nombre(), request.apellido(), request.email(),
				passwordEncoder.encode(request.password()), Rol.ACADEMICO);
		usuarioRepository.save(usuario);
		return new AuthResponse(jwtService.generarToken(usuario), toUsuarioResponse(usuario));
	}

	public void logout(String authorizationHeader) {
		String token = extraerToken(authorizationHeader);
		Claims claims = jwtService.parseToken(token).getPayload();
		tokenInvalidadoRepository.save(new TokenInvalidado(claims.getId(), Long.valueOf(claims.getSubject()),
				claims.getExpiration().toInstant().atOffset(ZoneOffset.UTC)));
	}

	private Usuario autenticar(String email, String password) {
		Usuario usuario = usuarioRepository.findByEmail(email)
				.filter(candidato -> candidato.getEstado() == EstadoUsuario.ACTIVO)
				.orElseThrow(() -> new UnauthorizedException(CREDENCIALES_INVALIDAS));
		if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
			throw new UnauthorizedException(CREDENCIALES_INVALIDAS);
		}
		return usuario;
	}

	private AuthResponse construirRespuestaConAcceso(Usuario usuario) {
		usuario.setUltimoAcceso(OffsetDateTime.now());
		usuarioRepository.save(usuario);
		return new AuthResponse(jwtService.generarToken(usuario), toUsuarioResponse(usuario));
	}

	private String extraerToken(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			throw new UnauthorizedException(TOKEN_INVALIDO);
		}
		return authorizationHeader.substring(BEARER_PREFIX.length());
	}

	private UsuarioResponse toUsuarioResponse(Usuario usuario) {
		String inicial = usuario.getNombre().substring(0, 1).toUpperCase();
		return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(), inicial,
				usuario.getRol().name().toLowerCase());
	}
}
