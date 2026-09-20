package com.kimngeam.backend.shared.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Selector del proveedor de correo activo, más el remitente. Se llama
 * {@code MailProviderProperties} (no {@code MailProperties}) para no chocar
 * con la clase propia de Spring Boot bajo el mismo nombre (la de
 * {@code spring.mail.*}, que gobierna host/puerto/usuario/password del SMTP).
 * Default {@code log}: un clon nuevo del repo debe poder levantar el flujo de
 * invitación sin credenciales de correo — ver {@link LoggingMailSender}.
 */
@ConfigurationProperties(prefix = "kimngeam.mail")
public record MailProviderProperties(@DefaultValue("log") MailProvider provider,
		@DefaultValue("no-reply@kimngeam.cl") String remitente) {
}
