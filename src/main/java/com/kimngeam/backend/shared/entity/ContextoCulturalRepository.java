package com.kimngeam.backend.shared.entity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContextoCulturalRepository extends JpaRepository<ContextoCultural, Long> {

	List<ContextoCultural> findByTraduccion_Id(String traduccionId);
}
