package it.at.restfs.auth;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import it.at.restfs.storage.dto.Container;

public class BasicAuth implements AuthorizationChecker, AuthorizationMaker {

	@Override
	public boolean isTokenValid(UUID container, Optional<String> authorization) {
		if (!authorization.isPresent()) {
			return false;
		}
				
		final String user = ""; //XXX get from auth config file
		final String pwd = ""; //XXX get from auth config file		
		final String current = build(user, pwd);
		
		return StringUtils.equals(current, authorization.get());
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
