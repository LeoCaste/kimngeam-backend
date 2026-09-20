package com.kimngeam.backend.admin.validaciones;

import com.kimngeam.backend.admin.validaciones.dto.UsuarioResumenResponse;
import com.kimngeam.backend.admin.validaciones.dto.ValidacionAdminResponse;
import com.kimngeam.backend.admin.validaciones.dto.ValidacionesAdminResponse;
import com.kimngeam.backend.auth.dto.MessageResponse;
import com.kimngeam.backend.shared.error.BadRequestException;
import com.kimngeam.backend.shared.util.Iniciales;
import com.kimngeam.backend.validaciones.ValidacionCorpusIndexer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * El listado va por JDBC directo con filtros opcionales combinables
 * ({@code tipo}, {@code usuario_id}, {@code busqueda}) — mismo criterio que
 * {@code admin.usuarios.UsuarioAdminService}: siempre parametrizado, nunca
 * concatenando el valor del usuario directo en el SQL.
 */
@Service
public class ValidacionAdminService {

	private static final String SQL_BASE = """
			SELECT v.id, v.expresion, v.texto, v.variante, v.fecha, u.id AS usuario_id, u.nombre AS usuario_nombre
			FROM validacion v
			JOIN usuario u ON u.id = v.usuario_id
			""";

	private final JdbcTemplate jdbcTemplate;
	private final ValidacionCorpusIndexer validacionCorpusIndexer;

	public ValidacionAdminService(JdbcTemplate jdbcTemplate, ValidacionCorpusIndexer validacionCorpusIndexer) {
		this.jdbcTemplate = jdbcTemplate;
		this.validacionCorpusIndexer = validacionCorpusIndexer;
	}

	public ValidacionesAdminResponse listar(String tipoParam, Long usuarioId, String busqueda) {
		TipoValidacion tipo = parsearTipo(tipoParam);

		List<String> condiciones = new ArrayList<>();
		List<Object> parametros = new ArrayList<>();
		if (tipo != null) {
			condiciones.add(tipo == TipoValidacion.GENERAL ? "v.expresion IS NULL" : "v.expresion IS NOT NULL");
		}
		if (usuarioId != null) {
			condiciones.add("v.usuario_id = ?");
			parametros.add(usuarioId);
		}
		if (busqueda != null && !busqueda.isBlank()) {
			condiciones.add("(v.expresion ILIKE ? OR v.texto ILIKE ?)");
			String patron = "%" + busqueda + "%";
			parametros.add(patron);
			parametros.add(patron);
		}

		StringBuilder sql = new StringBuilder(SQL_BASE);
		if (!condiciones.isEmpty()) {
			sql.append(" WHERE ").append(String.join(" AND ", condiciones));
		}
		sql.append(" ORDER BY v.fecha DESC");

		List<ValidacionAdminResponse> validaciones = jdbcTemplate.query(sql.toString(),
				ps -> vincularParametros(ps, parametros), this::mapearFila);
		return new ValidacionesAdminResponse(validaciones, validaciones.size());
	}

	public MessageResponse revertir(String validacionId) {
		validacionCorpusIndexer.revertir(validacionId);
		return new MessageResponse("Validación revertida correctamente");
	}

	private TipoValidacion parsearTipo(String tipoParam) {
		if (tipoParam == null) {
			return null;
		}
		try {
			return TipoValidacion.fromValue(tipoParam);
		}
		catch (IllegalArgumentException e) {
			throw new BadRequestException("El parámetro tipo debe ser \"expresion\" o \"general\"");
		}
	}

	private void vincularParametros(PreparedStatement statement, List<Object> parametros) throws SQLException {
		for (int i = 0; i < parametros.size(); i++) {
			statement.setObject(i + 1, parametros.get(i));
		}
	}

	private ValidacionAdminResponse mapearFila(ResultSet resultSet, int rowNum) throws SQLException {
		String expresion = resultSet.getString("expresion");
		String nombreUsuario = resultSet.getString("usuario_nombre");
		UsuarioResumenResponse usuario = new UsuarioResumenResponse(resultSet.getLong("usuario_id"), nombreUsuario,
				Iniciales.de(nombreUsuario));
		return new ValidacionAdminResponse(resultSet.getString("id"), usuario, expresion,
				resultSet.getString("texto"), TipoValidacion.desde(expresion).toValue(),
				resultSet.getString("variante"), resultSet.getObject("fecha", OffsetDateTime.class));
	}
}
