package com.helpdesk.cursohd.security.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.helpdesk.cursohd.security.entities.User;

@Service
public class TokenService {

	@Value("${api.security.token.secret}")
	private String secret;

	public String generateToken(User authenticatedUser) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);

			return JWT.create().withIssuer("login-auth-api").withSubject(authenticatedUser.getId().toString())
					.withClaim("email", authenticatedUser.getEmail()) // inclui e-mail como claim
					.withExpiresAt(this.generateExpirationDate()).sign(algorithm);

		} catch (JWTCreationException exception) {
			throw new RuntimeException("Erro ao gerar o token", exception);
		}
	}

	public String validateToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			DecodedJWT jwt = JWT.require(algorithm).withIssuer("login-auth-api").build().verify(token);

			return jwt.getSubject();

		} catch (JWTVerificationException exception) {
			return null;
		}
	}

	public String getEmailFromToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			DecodedJWT jwt = JWT.require(algorithm).withIssuer("login-auth-api").build().verify(token);

			return jwt.getClaim("email").asString();

		} catch (JWTVerificationException exception) {
			return null;
		}
	}

	private Instant generateExpirationDate() {
		return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
	}
}
