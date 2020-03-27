package it.at.restfs.auth;

import java.util.Optional;
import java.util.UUID;

import it.at.restfs.storage.dto.Container;

public class OAuth2 implements AuthorizationChecker, AuthorizationMaker {
	
	/*
	 * XXX add real implementation
	 */

	@Override
	public boolean isTokenValid(UUID container, Optional<String> authorization) {
		return Boolean.FALSE;
	}

	@Override
	public String creteToken(Container container) {
		return "42";
	}

}
