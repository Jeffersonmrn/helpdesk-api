package com.helpdesk.cursohd.resources;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.cursohd.dto.RegisterRequestDTO;
import com.helpdesk.cursohd.dto.ResponseDTO;
import com.helpdesk.cursohd.dto.UserDTO;
import com.helpdesk.cursohd.dto.UserPublicDTO;
import com.helpdesk.cursohd.dto.UserResponseDTO;
import com.helpdesk.cursohd.dto.UserUpdateDTO;
import com.helpdesk.cursohd.enums.ProfileEnum;
import com.helpdesk.cursohd.repositories.UserRepository;
import com.helpdesk.cursohd.response.Response;
import com.helpdesk.cursohd.security.entities.User;
import com.helpdesk.cursohd.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserResource {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<?> register(@RequestBody RegisterRequestDTO body) {
		ProfileEnum profile = null;
		try {
			profile = ProfileEnum.valueOf(body.profile().name());
		} catch (IllegalArgumentException | NullPointerException e) {
			return ResponseEntity.badRequest().body("Perfil (role) inválido.");
		}

		return registerUserWithRole(body, profile);
	}

	private ResponseEntity<?> registerUserWithRole(RegisterRequestDTO body, ProfileEnum profile) {
		Optional<User> userExists = userRepository.findByEmail(body.email());
		if (userExists.isPresent()) {
			Response<Void> response = new Response<>();
			response.getErrors().add("Email já cadastrado");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
		}

		User newUser = new User();
		newUser.setEmail(body.email());
		newUser.setPassword(passwordEncoder.encode(body.password()));
		newUser.setProfile(profile);

		userRepository.save(newUser);

		UserPublicDTO userDTO = new UserPublicDTO(newUser);

		Response<UserPublicDTO> response = new Response<>();
		response.setData(userDTO);
		return ResponseEntity.ok(response);
	}

	@PutMapping
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<?> update(@RequestBody UserUpdateDTO body) {
		Optional<User> userOpt = userRepository.findById(body.id());
		if (userOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
		}

		Optional<User> existingEmailUser = userRepository.findByEmail(body.email());
		if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(body.id())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já está em uso por outro usuário");
		}

		ProfileEnum profile;
		try {
			profile = ProfileEnum.valueOf(body.profile().name());
		} catch (IllegalArgumentException | NullPointerException e) {
			return ResponseEntity.badRequest().body("Perfil (role) inválido.");
		}

		User user = userOpt.get();
		user.setEmail(body.email());
		user.setPassword(passwordEncoder.encode(body.password()));
		user.setProfile(profile);

		userRepository.save(user);

		UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), null, user.getProfile());
		return ResponseEntity.ok(new ResponseDTO(userDTO, null));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
		return userRepository.findById(id).map(user -> ResponseEntity.ok(new UserResponseDTO(user)))
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(value = "{page}/{count}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<Page<UserResponseDTO>>> findAll(@PathVariable int page, @PathVariable int count) {
		Response<Page<UserResponseDTO>> response = new Response<>();

		Page<User> users = userService.findAll(page, count);

		Page<UserResponseDTO> dtoPage = users.map(UserResponseDTO::new);

		response.setData(dtoPage);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Void> deleteUser(@PathVariable String id) {
		return userRepository.findById(id).<ResponseEntity<Void>>map(user -> {
			userRepository.delete(user);
			return ResponseEntity.noContent().build();
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}
}
