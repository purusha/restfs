package it.at.restfs.auth;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

public class NoAuthorization implements AuthorizationChecker {

	@Override
	public boolean isTokenValid(UUID container, String authorization) {
		return StringUtils.isBlank(authorization);
	}

}
