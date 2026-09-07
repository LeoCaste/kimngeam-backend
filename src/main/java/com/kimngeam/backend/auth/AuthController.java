package com.kimngeam.backend.auth;

import com.kimngeam.backend.auth.dto.AuthResponse;
import com.kimngeam.backend.auth.dto.LoginRequest;
import com.kimngeam.backend.auth.dto.MessageResponse;
import com.kimngeam.backend.auth.dto.RegistroRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/login-admin")
	public ResponseEntity<AuthResponse> loginAdmin(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.loginAdmin(request));
	}

	@PostMapping("/registro")
	public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
	}

	@PostMapping("/logout")
	public ResponseEntity<MessageResponse> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
		authService.logout(authorizationHeader);
		return ResponseEntity.ok(new MessageResponse("Sesión cerrada correctamente"));
	}
}
