package com.kimngeam.backend.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.kimngeam.backend.shared.entity.TokenInvalidado;
import com.kimngeam.backend.shared.entity.TokenInvalidadoRepository;
import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.UsuarioRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code token_invalidado} crece con cada logout y nunca se purga sola (ver
 * CLAUDE.md, Fase 6): confirma que el job borra solo las filas ya expiradas,
 * dejando intactas las que todavía son parte válida de la blacklist.
 */
@SpringBootTest
@Transactional
class TokenInvalidadoCleanupJobTest {

	private static final String EMAIL_ACADEMICO = "tefi@ufro.cl";

	@Autowired
	private TokenInvalidadoCleanupJob cleanupJob;

	@Autowired
	private TokenInvalidadoRepository tokenInvalidadoRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Test
	void purgaSoloLasFilasExpiradas() {
		Long usuarioId = usuarioRepository.findByEmail(EMAIL_ACADEMICO).orElseThrow().getId();
		String jtiExpirado = UUID.randomUUID().toString();
		String jtiVigente = UUID.randomUUID().toString();
		tokenInvalidadoRepository
				.save(new TokenInvalidado(jtiExpirado, usuarioId, OffsetDateTime.now().minusDays(1)));
		tokenInvalidadoRepository.save(new TokenInvalidado(jtiVigente, usuarioId, OffsetDateTime.now().plusDays(1)));

		cleanupJob.purgarExpirados();

		assertThat(tokenInvalidadoRepository.existsById(jtiExpirado)).isFalse();
		assertThat(tokenInvalidadoRepository.existsById(jtiVigente)).isTrue();
	}
}
