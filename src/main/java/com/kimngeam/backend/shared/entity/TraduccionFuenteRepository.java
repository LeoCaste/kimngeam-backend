package com.kimngeam.backend.shared.entity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraduccionFuenteRepository extends JpaRepository<TraduccionFuente, Long> {

	List<TraduccionFuente> findBySegmentoId(Long segmentoId);
}
