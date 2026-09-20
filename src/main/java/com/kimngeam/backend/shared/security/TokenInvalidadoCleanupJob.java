package com.kimngeam.backend.shared.security;

import com.kimngeam.backend.shared.entity.TokenInvalidadoRepository;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@code token_invalidado} crece con cada logout y nunca se purga sola:
 * este job borra las filas cuyo {@code expira_en} ya pasó — el propio JWT ya
 * expiró para esas filas, así que mantenerlas en la blacklist no aporta nada
 * (ver CLAUDE.md, Fase 6). Frecuencia configurable vía
 * {@link TokenInvalidadoCleanupProperties}.
 */
@Component
public class TokenInvalidadoCleanupJob {

	private static final Logger log = LoggerFactory.getLogger(TokenInvalidadoCleanupJob.class);

	private final TokenInvalidadoRepository tokenInvalidadoRepository;

	public TokenInvalidadoCleanupJob(TokenInvalidadoRepository tokenInvalidadoRepository) {
		this.tokenInvalidadoRepository = tokenInvalidadoRepository;
	}

	@Scheduled(fixedDelayString = "${kimngeam.auth.token-invalidado-cleanup.intervalo:1h}")
	public void purgarExpirados() {
		int eliminados = tokenInvalidadoRepository.eliminarExpiradosAntesDe(OffsetDateTime.now());
		if (eliminados > 0) {
			log.info("token_invalidado: {} fila(s) expirada(s) purgada(s)", eliminados);
		}
	}
}
