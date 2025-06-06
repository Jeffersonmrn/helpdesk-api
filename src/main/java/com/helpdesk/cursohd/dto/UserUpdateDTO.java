package com.helpdesk.cursohd.dto;

import com.helpdesk.cursohd.enums.ProfileEnum;

public record UserUpdateDTO(String id, String email, String password, ProfileEnum profile) {
}
