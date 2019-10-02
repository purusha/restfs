package it.at.restfs.auth;

import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoAuthorization implements AuthorizationChecker {

	@Override
	public boolean isTokenValid(UUID container, Optional<String> authorization) {		
		return !authorization.isPresent();
	}

}
