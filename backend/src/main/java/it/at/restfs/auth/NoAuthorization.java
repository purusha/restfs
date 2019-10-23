package it.at.restfs.auth;

import java.util.Optional;
import java.util.UUID;

import it.at.restfs.storage.dto.Container;

public class NoAuthorization implements AuthorizationChecker, AuthorizationMaker {

	@Override
	public boolean isTokenValid(UUID container, Optional<String> authorization) {		
		return !authorization.isPresent();
	}

	@Override
	public String creteToken(Container container) {
		throw new RuntimeException("cannot generate token for container " + container.getId()); 
	}

}
