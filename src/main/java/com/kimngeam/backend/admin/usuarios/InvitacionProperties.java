package com.kimngeam.backend.admin.usuarios;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "kimngeam.admin.invitaciones")
public record InvitacionProperties(@DefaultValue("7d") Duration expiracion) {
}
