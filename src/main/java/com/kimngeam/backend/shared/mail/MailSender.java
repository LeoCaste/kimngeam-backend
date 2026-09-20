package com.kimngeam.backend.shared.mail;

/**
 * Puerto hacia el envío de correo. Dos implementaciones (ver
 * {@code kimngeam.mail.provider}), mismo mecanismo que el proveedor de
 * embeddings y el de LLM: {@link LoggingMailSender} (default, no envía nada
 * de verdad) y {@link SmtpMailSender}.
 */
public interface MailSender {

	void enviar(String destinatario, String asunto, String cuerpo);
}
