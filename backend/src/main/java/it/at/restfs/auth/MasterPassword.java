package it.at.restfs.auth;

public class MasterPassword implements AuthorizationChecker {

	@Override
	public boolean isTokenValid(String authorization) {
		return Boolean.TRUE;
	}

}
