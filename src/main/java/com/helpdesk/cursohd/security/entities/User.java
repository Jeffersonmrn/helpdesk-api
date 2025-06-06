package com.helpdesk.cursohd.security.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import com.helpdesk.cursohd.enums.ProfileEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Document
public class User {

	@Id
	private String id;

	@Indexed(unique = true)
	@NotBlank(message = "Email required")
	@Email(message = "Email invalid")
	private String email;

	@NotBlank(message = "Password required")
	@Size(min = 6)
	private String password;

	@Field(targetType = FieldType.STRING)
	private ProfileEnum profile;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setProfile(ProfileEnum profile) {
		this.profile = profile;
	}

	public ProfileEnum getProfile() {
		return profile;
	}
}
