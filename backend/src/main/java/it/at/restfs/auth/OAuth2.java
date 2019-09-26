package it.at.restfs.auth;

import java.util.UUID;

public class OAuth2 implements AuthorizationChecker {

	@Override
	public boolean isTokenValid(UUID container, String authorization) {
		return Boolean.TRUE;
	}

}
