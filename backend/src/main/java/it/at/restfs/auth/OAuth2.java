package it.at.restfs.auth;

import java.util.Optional;
import java.util.UUID;

public class OAuth2 implements AuthorizationChecker {

	@Override
	public boolean isTokenValid(UUID container, Optional<String> authorization) {
		return Boolean.TRUE;
	}

}
