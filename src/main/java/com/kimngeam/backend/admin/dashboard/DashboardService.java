package com.kimngeam.backend.admin.dashboard;

import com.kimngeam.backend.admin.dashboard.dto.DashboardResponse;
import com.kimngeam.backend.admin.dashboard.dto.ExpresionesResponse;
import com.kimngeam.backend.admin.dashboard.dto.UltimaValidacionResponse;
import com.kimngeam.backend.admin.dashboard.dto.UsuarioBreveResponse;
import com.kimngeam.backend.admin.dashboard.dto.VarianteCoberturaResponse;
import com.kimngeam.backend.admin.validaciones.TipoValidacion;
import com.kimngeam.backend.shared.entity.Direccion;
import com.kimngeam.backend.shared.util.Iniciales;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Todos los agregados salen por JDBC directo (mismo criterio que
 * {@code admin.usuarios.UsuarioAdminService} y {@code admin.validaciones.ValidacionAdminService}):
 * ninguno tiene tabla propia, se derivan en cada consulta.
 *
 * <p>
 * Semántica de las métricas ambiguas del contrato, acordada explícitamente
 * con el usuario (ver CLAUDE.md, Fase 5):
 * <ul>
 * <li>{@code expresiones.total}, {@code con_contexto} y {@code sin_contexto}
 * se calculan sobre traducciones {@code es-map} (único sentido donde existe
 * contexto cultural). {@code sin_validar} se calcula sobre traducciones de
 * ambas direcciones. Los cuatro son conteos independientes, no una partición
 * de {@code total}.</li>
 * <li>{@code variantes[].porcentaje} es la participación de esa variante
 * sobre el total de validaciones ({@code cantidad / total_validaciones * 100}),
 * NO la cobertura real del corpus para esa variante — ver
 * {@link VarianteCoberturaResponse}.</li>
 * </ul>
 */
@Service
public class DashboardService {

	private static final String SQL_TOTALES = """
			SELECT
			    (SELECT COUNT(*) FROM usuario) AS total_usuarios,
			    (SELECT COUNT(*) FROM traduccion) AS total_traducciones,
			    (SELECT COUNT(*) FROM validacion) AS total_validaciones
			""";

	private static final String SQL_EXPRESIONES = """
			SELECT
			    (SELECT COUNT(*) FROM traduccion WHERE direccion = ?) AS total,
			    (SELECT COUNT(DISTINCT t.id) FROM traduccion t
			         JOIN contexto_cultural c ON c.traduccion_id = t.id
			         WHERE t.direccion = ?) AS con_contexto,
			    (SELECT COUNT(*) FROM traduccion t
			         WHERE t.direccion = ?
			         AND NOT EXISTS (SELECT 1 FROM contexto_cultural c WHERE c.traduccion_id = t.id)) AS sin_contexto,
			    (SELECT COUNT(*) FROM traduccion t
			         WHERE NOT EXISTS (SELECT 1 FROM validacion v WHERE v.traduccion_id = t.id)) AS sin_validar
			""";

	private static final String SQL_VARIANTES = """
			SELECT COALESCE(variante, 'Otros') AS nombre, COUNT(*) AS cantidad
			FROM validacion
			GROUP BY COALESCE(variante, 'Otros')
			ORDER BY cantidad DESC
			""";

	private static final String SQL_ULTIMAS_VALIDACIONES = """
			SELECT v.id, v.expresion, v.texto, v.variante, v.fecha, u.nombre AS usuario_nombre
			FROM validacion v
			JOIN usuario u ON u.id = v.usuario_id
			ORDER BY v.fecha DESC
			LIMIT ?
			""";

	private final JdbcTemplate jdbcTemplate;
	private final DashboardProperties dashboardProperties;

	public DashboardService(JdbcTemplate jdbcTemplate, DashboardProperties dashboardProperties) {
		this.jdbcTemplate = jdbcTemplate;
		this.dashboardProperties = dashboardProperties;
	}

	public DashboardResponse obtener() {
		long[] totales = jdbcTemplate.queryForObject(SQL_TOTALES, this::mapearTotales);
		ExpresionesResponse expresiones = obtenerExpresiones();
		List<VarianteCoberturaResponse> variantes = obtenerVariantes(totales[2]);
		List<UltimaValidacionResponse> ultimasValidaciones = jdbcTemplate.query(SQL_ULTIMAS_VALIDACIONES,
				ps -> ps.setInt(1, dashboardProperties.ultimasValidacionesLimite()), this::mapearUltimaValidacion);

		return new DashboardResponse(totales[0], totales[1], totales[2], expresiones, variantes,
				ultimasValidaciones);
	}

	private ExpresionesResponse obtenerExpresiones() {
		String esMap = Direccion.ES_MAP.toValue();
		return jdbcTemplate.queryForObject(SQL_EXPRESIONES,
				(resultSet, rowNum) -> new ExpresionesResponse(resultSet.getLong("total"),
						resultSet.getLong("con_contexto"), resultSet.getLong("sin_validar"),
						resultSet.getLong("sin_contexto")),
				esMap, esMap, esMap);
	}

	private List<VarianteCoberturaResponse> obtenerVariantes(long totalValidaciones) {
		return jdbcTemplate.query(SQL_VARIANTES, (resultSet, rowNum) -> {
			long cantidad = resultSet.getLong("cantidad");
			int porcentaje = totalValidaciones == 0 ? 0 : Math.round(cantidad * 100f / totalValidaciones);
			return new VarianteCoberturaResponse(resultSet.getString("nombre"), cantidad, porcentaje);
		});
	}

	private long[] mapearTotales(ResultSet resultSet, int rowNum) throws SQLException {
		return new long[] { resultSet.getLong("total_usuarios"), resultSet.getLong("total_traducciones"),
				resultSet.getLong("total_validaciones") };
	}

	private UltimaValidacionResponse mapearUltimaValidacion(ResultSet resultSet, int rowNum) throws SQLException {
		String expresion = resultSet.getString("expresion");
		String nombreUsuario = resultSet.getString("usuario_nombre");
		UsuarioBreveResponse usuario = new UsuarioBreveResponse(nombreUsuario, Iniciales.de(nombreUsuario));
		return new UltimaValidacionResponse(resultSet.getString("id"), usuario, expresion, resultSet.getString("texto"),
				TipoValidacion.desde(expresion).toValue(), resultSet.getString("variante"),
				resultSet.getObject("fecha", OffsetDateTime.class));
	}
}
