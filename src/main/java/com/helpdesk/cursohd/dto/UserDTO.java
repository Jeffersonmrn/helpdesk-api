package com.helpdesk.cursohd.dto;

import com.helpdesk.cursohd.enums.ProfileEnum;
import com.helpdesk.cursohd.security.entities.User;

public record UserDTO(String id, String email, String password, ProfileEnum profile) {

	public static UserDTO fromEntity(User user) {
		return new UserDTO(user.getId(), user.getEmail(), user.getPassword(), user.getProfile());
	}
}
