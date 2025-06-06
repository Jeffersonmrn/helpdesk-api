package com.helpdesk.cursohd.security.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.cursohd.dto.LoginRequestDTO;
import com.helpdesk.cursohd.dto.ResponseDTO;
import com.helpdesk.cursohd.dto.UserDTO;
import com.helpdesk.cursohd.repositories.UserRepository;
import com.helpdesk.cursohd.security.entities.User;
import com.helpdesk.cursohd.security.service.TokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthResource {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;

	@PostMapping
	public ResponseEntity<?> login(@RequestBody LoginRequestDTO body) {
		User user = userRepository.findByEmail(body.email())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

		if (passwordEncoder.matches(body.password(), user.getPassword())) {
			String token = tokenService.generateToken(user);

			UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), null, user.getProfile());

			return ResponseEntity.ok(new ResponseDTO(userDTO, token));
		}

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
	}
}
