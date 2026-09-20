package com.kimngeam.backend.shared.mail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * SMTP genérico, no la API REST de un proveedor específico: lo habla tanto
 * el servidor institucional de la UFRO como los servicios transaccionales
 * (donde la API key se usa como password SMTP), así que cambiar de proveedor
 * de correo es solo configuración (host/puerto/usuario/password vía
 * {@code spring.mail.*}, estándar de Spring Boot — variables de entorno
 * {@code SPRING_MAIL_HOST}, etc.), nunca un cambio de código.
 */
@Component
@ConditionalOnProperty(prefix = "kimngeam.mail", name = "provider", havingValue = "smtp")
public class SmtpMailSender implements MailSender {

	private final JavaMailSender javaMailSender;
	private final MailProviderProperties properties;

	public SmtpMailSender(JavaMailSender javaMailSender, MailProviderProperties properties) {
		this.javaMailSender = javaMailSender;
		this.properties = properties;
	}

	@Override
	public void enviar(String destinatario, String asunto, String cuerpo) {
		SimpleMailMessage mensaje = new SimpleMailMessage();
		mensaje.setFrom(properties.remitente());
		mensaje.setTo(destinatario);
		mensaje.setSubject(asunto);
		mensaje.setText(cuerpo);
		javaMailSender.send(mensaje);
	}
}
