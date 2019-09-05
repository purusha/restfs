package it.at.restfs.auth;

public class OAuth2 implements AuthorizationChecker {

	@Override
	public boolean isTokenValid(String authorization) {
		return Boolean.TRUE;
	}

}
