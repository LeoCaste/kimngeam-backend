package com.kimngeam.backend.validaciones;

import com.kimngeam.backend.shared.entity.Traduccion;
import com.kimngeam.backend.shared.entity.TraduccionRepository;
import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.UsuarioRepository;
import com.kimngeam.backend.shared.entity.Validacion;
import com.kimngeam.backend.shared.entity.ValidacionRepository;
import com.kimngeam.backend.shared.error.NotFoundException;
import com.kimngeam.backend.validaciones.dto.ValidacionResponse;
import com.kimngeam.backend.validaciones.dto.ValidarExpresionRequest;
import com.kimngeam.backend.validaciones.dto.ValidarGeneralRequest;
import org.springframework.stereotype.Service;

/**
 * El campo {@code tipo} del contrato ({@code "expresion"} / {@code "general"})
 * nunca se persiste: se deriva de si {@code expresion} es {@code null}
 * (ver docs/API_CONTRACTS.md).
 */
@Service
public class ValidacionService {

	private static final String MENSAJE_EXPRESION = "Validación registrada correctamente";
	private static final String MENSAJE_GENERAL = "Validación general registrada correctamente";

	private final ValidacionRepository validacionRepository;
	private final TraduccionRepository traduccionRepository;
	private final UsuarioRepository usuarioRepository;
	private final ValidacionCorpusIndexer validacionCorpusIndexer;

	public ValidacionService(ValidacionRepository validacionRepository, TraduccionRepository traduccionRepository,
			UsuarioRepository usuarioRepository, ValidacionCorpusIndexer validacionCorpusIndexer) {
		this.validacionRepository = validacionRepository;
		this.traduccionRepository = traduccionRepository;
		this.usuarioRepository = usuarioRepository;
		this.validacionCorpusIndexer = validacionCorpusIndexer;
	}

	public ValidacionResponse registrarExpresion(ValidarExpresionRequest request, Long usuarioId) {
		Validacion validacion = registrar(request.traduccionId(), usuarioId, request.expresion(),
				request.comentario(), request.variante());
		return new ValidacionResponse(validacion.getId(), MENSAJE_EXPRESION);
	}

	public ValidacionResponse registrarGeneral(ValidarGeneralRequest request, Long usuarioId) {
		Validacion validacion = registrar(request.traduccionId(), usuarioId, null, request.comentario(), null);
		return new ValidacionResponse(validacion.getId(), MENSAJE_GENERAL);
	}

	private Validacion registrar(String traduccionId, Long usuarioId, String expresion, String texto,
			String variante) {
		Traduccion traduccion = traduccionRepository.findById(traduccionId)
				.orElseThrow(() -> new NotFoundException("No existe una traducción con id " + traduccionId));
		Usuario academico = usuarioRepository.getReferenceById(usuarioId);

		Validacion validacion = validacionRepository
				.save(new Validacion(academico, traduccion, expresion, texto, variante));
		validacionCorpusIndexer.indexar(validacion, academico);
		return validacion;
	}
}
