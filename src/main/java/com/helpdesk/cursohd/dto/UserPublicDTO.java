package com.helpdesk.cursohd.dto;

import com.helpdesk.cursohd.enums.ProfileEnum;
import com.helpdesk.cursohd.security.entities.User;

public record UserPublicDTO(String id, String email, ProfileEnum profile) {
    public UserPublicDTO(UserDTO user) {
        this(user.id(), user.email(), user.profile());
    }
    
    public UserPublicDTO(User user) {
        this(user.getId(), user.getEmail(), user.getProfile());
    }
}
