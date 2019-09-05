package it.at.restfs.auth;

public class NoAuthorization implements AuthorizationChecker {

	@Override
	public boolean isTokenValid(String authorization) {
		return Boolean.TRUE;
	}

}
