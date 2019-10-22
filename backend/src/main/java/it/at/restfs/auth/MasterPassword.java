package it.at.restfs.auth;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class MasterPassword implements AuthorizationChecker {

	@Override
	public boolean isTokenValid(UUID container, Optional<String> authorization) {
		return authorization.isPresent() && Objects.nonNull(UUID.fromString(authorization.get()));
	}
	
}
