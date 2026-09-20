package com.kimngeam.backend.shared.entity;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CorpusChunkRepository extends JpaRepository<CorpusChunk, UUID> {

	/**
	 * Spring Data implementa los métodos derivados {@code deleteBy...}
	 * cargando las entidades y llamando {@code remove()} una por una, lo que
	 * exige una transacción activa — a diferencia de {@code saveAll}, que ya
	 * la trae por venir de {@code SimpleJpaRepository}.
	 */
	@Transactional
	void deleteBySourceType(SourceType sourceType);

	/**
	 * Ubica el chunk que una validación generó, para poder revertirlo (ver
	 * {@code validaciones.ValidacionCorpusIndexer#revertir}).
	 */
	Optional<CorpusChunk> findBySourceTypeAndSourceRef(SourceType sourceType, String sourceRef);
}
