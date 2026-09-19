package com.kimngeam.backend.shared.entity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraduccionSegmentoRepository extends JpaRepository<TraduccionSegmento, Long> {

	List<TraduccionSegmento> findByTraduccionIdOrderByOrden(String traduccionId);
}
