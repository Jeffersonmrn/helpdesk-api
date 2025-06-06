package com.helpdesk.cursohd.dto;

import com.helpdesk.cursohd.security.entities.User;

public class UserResponseDTO {
	private String id;
	private String email;
	private String profile;

	public UserResponseDTO(User user) {
		this.id = user.getId();
		this.email = user.getEmail();
		this.profile = user.getProfile() != null ? user.getProfile().name() : null;
	}

	public String getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getProfile() {
		return profile;
	}
}
