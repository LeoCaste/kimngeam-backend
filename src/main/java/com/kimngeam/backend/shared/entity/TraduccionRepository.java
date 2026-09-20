package com.kimngeam.backend.shared.entity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraduccionRepository extends JpaRepository<Traduccion, String> {

	List<Traduccion> findByUsuario_IdOrderByFechaDesc(Long usuarioId);
}
