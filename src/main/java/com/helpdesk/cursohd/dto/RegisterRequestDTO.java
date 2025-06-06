package com.helpdesk.cursohd.dto;

import com.helpdesk.cursohd.enums.ProfileEnum;

public record RegisterRequestDTO(String name, String email, String password, ProfileEnum profile) {
}
