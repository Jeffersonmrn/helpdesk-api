package com.helpdesk.cursohd.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.helpdesk.cursohd.security.entities.User;

public interface UserRepository extends MongoRepository<User, String> {

boolean existsByEmail(String email);
	
	Optional<User> findByEmail(String email);
}