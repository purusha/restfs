package it.at.restfs.auth;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.typesafe.config.Config;

import it.at.restfs.storage.dto.Container;

public class BasicAuth implements AuthorizationChecker, AuthorizationMaker {
	
	private final AuthorizationConfigHandler configResolver;

	@Inject
	public BasicAuth(AuthorizationConfigHandler configResolver) {
		this.configResolver = configResolver;
	}

	@Override
	public boolean isTokenValid(UUID container, Optional<String> authorization) {
		if (!authorization.isPresent()) {
			return false;
		}
		
		final Config config = configResolver.get(container);				
		final String user = config.getString("user");
		final String pwd = config.getString("pwd");		
		
		return StringUtils.equals(build(user, pwd), authorization.get());
	}

	private String build(String user, String pwd) {
		return Base64.getEncoder().encodeToString(
			(user + ":" + pwd).getBytes()
		);
	}

	@Override
	public String creteToken(Container container) {
		throw new RuntimeException("cannot generate token for container " + container.getId()); 		
	}

}
