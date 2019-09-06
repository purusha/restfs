package it.at.restfs.auth;

import org.apache.commons.lang3.StringUtils;

public class NoAuthorization implements AuthorizationChecker {

	@Override
	public boolean isTokenValid(String authorization) {
		return StringUtils.isBlank(authorization);
	}

}
