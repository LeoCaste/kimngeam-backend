package com.kimngeam.backend.shared.entity;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidacionRepository extends JpaRepository<Validacion, String> {

	/**
	 * {@code Pageable} en vez de un {@code findTop10By...} derivado: el
	 * límite de "validaciones recientes" es configurable
	 * ({@code kimngeam.admin.usuarios.validaciones-recientes-limite}), no un
	 * número fijo en el nombre del método (ver CLAUDE.md, "nada hardcodeado").
	 */
	List<Validacion> findByUsuario_IdOrderByFechaDesc(Long usuarioId, Pageable pageable);
}
