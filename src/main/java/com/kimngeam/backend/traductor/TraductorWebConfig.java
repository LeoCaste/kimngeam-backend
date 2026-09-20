package com.kimngeam.backend.traductor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TraductorWebConfig implements WebMvcConfigurer {

	private static final String RUTA_TRADUCIR = "/traductor/traducir";

	private final RateLimitInterceptor rateLimitInterceptor;

	public TraductorWebConfig(RateLimitInterceptor rateLimitInterceptor) {
		this.rateLimitInterceptor = rateLimitInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(rateLimitInterceptor).addPathPatterns(RUTA_TRADUCIR);
	}
}
