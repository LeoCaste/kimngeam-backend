package com.kimngeam.backend.historial;

import com.kimngeam.backend.historial.dto.HistorialItemResponse;
import com.kimngeam.backend.historial.dto.HistorialResponse;
import com.kimngeam.backend.shared.entity.Traduccion;
import com.kimngeam.backend.shared.entity.TraduccionRepository;
import org.springframework.stereotype.Service;

/**
 * Traducciones del usuario autenticado, siempre por {@code usuario_id} —
 * nunca por nombre (ver CLAUDE.md).
 */
@Service
public class HistorialService {

	private final TraduccionRepository traduccionRepository;

	public HistorialService(TraduccionRepository traduccionRepository) {
		this.traduccionRepository = traduccionRepository;
	}

	public HistorialResponse obtenerHistorial(Long usuarioId) {
		return new HistorialResponse(
				traduccionRepository.findByUsuario_IdOrderByFechaDesc(usuarioId).stream()
						.map(HistorialService::aItem)
						.toList());
	}

	private static HistorialItemResponse aItem(Traduccion traduccion) {
		return new HistorialItemResponse(traduccion.getId(), traduccion.getTextoOrigen(),
				traduccion.getTextoTraducido(), traduccion.getDireccion().toValue(), traduccion.getFecha());
	}
}
