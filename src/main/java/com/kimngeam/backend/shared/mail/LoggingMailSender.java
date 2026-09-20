package com.kimngeam.backend.shared.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Proveedor de correo por defecto: NO envía nada de verdad, solo loguea. Es
 * el default a propósito para que el flujo de invitación funcione sin
 * credenciales SMTP en un clon nuevo del repo — pero eso significa que hoy,
 * salvo que se configure {@code kimngeam.mail.provider=smtp}, ningún
 * académico invitado recibe un correo real. No asumir que el flujo de
 * invitación está completo solo porque el endpoint responde 200.
 */
@Component
@ConditionalOnProperty(prefix = "kimngeam.mail", name = "provider", havingValue = "log", matchIfMissing = true)
public class LoggingMailSender implements MailSender {

	private static final Logger log = LoggerFactory.getLogger(LoggingMailSender.class);

	@Override
	public void enviar(String destinatario, String asunto, String cuerpo) {
		log.info("Correo NO enviado (kimngeam.mail.provider=log) — para: {} | asunto: {} | cuerpo: {}", destinatario,
				asunto, cuerpo);
	}
}
