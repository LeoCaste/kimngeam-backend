package com.kimngeam.backend.auth;

import com.kimngeam.backend.shared.entity.Rol;
import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seed de desarrollo: un académico y un admin para poder probar los
 * endpoints de auth sin pasar por /auth/registro. Credenciales documentadas
 * en README.md. Solo corre bajo el perfil {@code dev}, nunca en producción.
 */
@Component
@Profile("dev")
public class DevUsuarioSeeder implements CommandLineRunner {

	private static final String EMAIL_ACADEMICO = "tefi@ufro.cl";
	private static final String PASSWORD_ACADEMICO = "password123";
	private static final String EMAIL_ADMIN = "admin@kimngeam.cl";
	private static final String PASSWORD_ADMIN = "admin1234";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public DevUsuarioSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		crearSiNoExiste(EMAIL_ACADEMICO, "Tefi", "Huenchual", PASSWORD_ACADEMICO, Rol.ACADEMICO);
		crearSiNoExiste(EMAIL_ADMIN, "Admin", "Kimngeam", PASSWORD_ADMIN, Rol.ADMIN);
	}

	private void crearSiNoExiste(String email, String nombre, String apellido, String password, Rol rol) {
		if (usuarioRepository.existsByEmail(email)) {
			return;
		}
		usuarioRepository.save(new Usuario(nombre, apellido, email, passwordEncoder.encode(password), rol));
	}
}
