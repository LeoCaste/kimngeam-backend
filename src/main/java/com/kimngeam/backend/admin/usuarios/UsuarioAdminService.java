package com.kimngeam.backend.admin.usuarios;

import com.kimngeam.backend.admin.usuarios.dto.CambiarEstadoRequest;
import com.kimngeam.backend.admin.usuarios.dto.InvitarUsuarioRequest;
import com.kimngeam.backend.admin.usuarios.dto.UsuarioAdminResponse;
import com.kimngeam.backend.admin.usuarios.dto.UsuarioDetalleResponse;
import com.kimngeam.backend.admin.usuarios.dto.UsuariosAdminResponse;
import com.kimngeam.backend.admin.usuarios.dto.ValidacionRecienteResponse;
import com.kimngeam.backend.admin.validaciones.TipoValidacion;
import com.kimngeam.backend.auth.dto.MessageResponse;
import com.kimngeam.backend.shared.entity.EstadoUsuario;
import com.kimngeam.backend.shared.entity.Invitacion;
import com.kimngeam.backend.shared.entity.InvitacionRepository;
import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.UsuarioRepository;
import com.kimngeam.backend.shared.entity.Validacion;
import com.kimngeam.backend.shared.entity.ValidacionRepository;
import com.kimngeam.backend.shared.error.ConflictException;
import com.kimngeam.backend.shared.error.NotFoundException;
import com.kimngeam.backend.shared.mail.MailSender;
import com.kimngeam.backend.shared.util.Iniciales;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * El listado y el detalle van por JDBC directo (mismo criterio que
 * {@code rag.retrieval.CorpusRetrievalService}): son consultas de solo
 * lectura con agregados (conteos de traducciones/validaciones por usuario)
 * que no ganan nada pasando por JPA.
 */
@Service
public class UsuarioAdminService {

	private static final String SQL_BASE = """
			SELECT u.id, u.nombre, u.email, u.estado, u.ultimo_acceso,
			       (SELECT COUNT(*) FROM traduccion t WHERE t.usuario_id = u.id) AS traducciones,
			       (SELECT COUNT(*) FROM validacion v WHERE v.usuario_id = u.id) AS validaciones
			FROM usuario u
			""";

	private static final String SQL_LISTAR = SQL_BASE + " ORDER BY u.id";

	private static final String SQL_DETALLE = SQL_BASE + " WHERE u.id = ?";

	private final JdbcTemplate jdbcTemplate;
	private final UsuarioRepository usuarioRepository;
	private final ValidacionRepository validacionRepository;
	private final InvitacionRepository invitacionRepository;
	private final MailSender mailSender;
	private final EstadoUsuarioCalculator estadoUsuarioCalculator;
	private final UmbralActividadProperties umbralActividadProperties;
	private final InvitacionProperties invitacionProperties;

	public UsuarioAdminService(JdbcTemplate jdbcTemplate, UsuarioRepository usuarioRepository,
			ValidacionRepository validacionRepository, InvitacionRepository invitacionRepository,
			MailSender mailSender, EstadoUsuarioCalculator estadoUsuarioCalculator,
			UmbralActividadProperties umbralActividadProperties, InvitacionProperties invitacionProperties) {
		this.jdbcTemplate = jdbcTemplate;
		this.usuarioRepository = usuarioRepository;
		this.validacionRepository = validacionRepository;
		this.invitacionRepository = invitacionRepository;
		this.mailSender = mailSender;
		this.estadoUsuarioCalculator = estadoUsuarioCalculator;
		this.umbralActividadProperties = umbralActividadProperties;
		this.invitacionProperties = invitacionProperties;
	}

	public UsuariosAdminResponse listar() {
		List<UsuarioAdminResponse> usuarios = jdbcTemplate.query(SQL_LISTAR, this::mapearFila);
		return new UsuariosAdminResponse(usuarios, usuarios.size());
	}

	public UsuarioDetalleResponse obtenerDetalle(Long usuarioId) {
		UsuarioAdminResponse usuario = jdbcTemplate
				.query(SQL_DETALLE, ps -> ps.setLong(1, usuarioId), this::mapearFila)
				.stream()
				.findFirst()
				.orElseThrow(() -> new NotFoundException("No existe un usuario con id " + usuarioId));

		List<ValidacionRecienteResponse> recientes = validacionRepository
				.findByUsuario_IdOrderByFechaDesc(usuarioId,
						PageRequest.of(0, umbralActividadProperties.validacionesRecientesLimite()))
				.stream()
				.map(UsuarioAdminService::aValidacionReciente)
				.toList();

		return new UsuarioDetalleResponse(usuario, recientes);
	}

	public MessageResponse invitar(InvitarUsuarioRequest request, Long adminId) {
		if (usuarioRepository.existsByEmail(request.email()) || invitacionRepository.existsByEmail(request.email())) {
			throw new ConflictException("EMAIL_TAKEN", "El correo ya está registrado");
		}
		Usuario admin = usuarioRepository.getReferenceById(adminId);
		String token = UUID.randomUUID().toString().replace("-", "");
		OffsetDateTime expiraEn = OffsetDateTime.now(ZoneOffset.UTC).plus(invitacionProperties.expiracion());
		invitacionRepository.save(new Invitacion(request.nombre(), request.email(), admin, token, expiraEn));

		mailSender.enviar(request.email(), "Invitación a Kimngeam-IA",
				"Hola " + request.nombre() + ", te invitaron a unirte a Kimngeam-IA como académico. "
						+ "Tu código de invitación es: " + token);

		return new MessageResponse("Invitación enviada a " + request.email());
	}

	public MessageResponse cambiarEstado(Long usuarioId, CambiarEstadoRequest request) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new NotFoundException("No existe un usuario con id " + usuarioId));
		usuario.setEstado(EstadoUsuario.valueOf(request.estado().toUpperCase()));
		usuarioRepository.save(usuario);
		return new MessageResponse("Estado actualizado correctamente");
	}

	private static ValidacionRecienteResponse aValidacionReciente(Validacion validacion) {
		String tipo = TipoValidacion.desde(validacion.getExpresion()).toValue();
		return new ValidacionRecienteResponse(validacion.getId(), validacion.getExpresion(), validacion.getTexto(),
				tipo, validacion.getVariante(), validacion.getFecha());
	}

	private UsuarioAdminResponse mapearFila(ResultSet resultSet, int rowNum) throws SQLException {
		String nombre = resultSet.getString("nombre");
		EstadoUsuario estadoPersistido = EstadoUsuario.valueOf(resultSet.getString("estado").toUpperCase());
		OffsetDateTime ultimoAcceso = resultSet.getObject("ultimo_acceso", OffsetDateTime.class);
		String estadoVista = estadoUsuarioCalculator.calcular(estadoPersistido, ultimoAcceso).toValue();
		return new UsuarioAdminResponse(resultSet.getLong("id"), nombre, resultSet.getString("email"),
				Iniciales.de(nombre), resultSet.getLong("traducciones"), resultSet.getLong("validaciones"),
				ultimoAcceso, estadoVista);
	}
}
