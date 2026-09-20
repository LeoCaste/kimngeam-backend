package com.kimngeam.backend.shared.entity;

import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TokenInvalidadoRepository extends JpaRepository<TokenInvalidado, String> {

	/**
	 * Una vez que el propio JWT expiró, {@link com.kimngeam.backend.shared.security.JwtAuthenticationFilter}
	 * ya lo rechaza por expiración antes de siquiera consultar esta tabla —
	 * mantener la fila no aporta nada (ver {@code TokenInvalidadoCleanupJob}).
	 */
	@Modifying
	@Transactional
	@Query("delete from TokenInvalidado t where t.expiraEn < :limite")
	int eliminarExpiradosAntesDe(@Param("limite") OffsetDateTime limite);
}
