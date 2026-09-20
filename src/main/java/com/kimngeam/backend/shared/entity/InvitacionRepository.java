package com.kimngeam.backend.shared.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitacionRepository extends JpaRepository<Invitacion, Long> {

	boolean existsByEmail(String email);
}
