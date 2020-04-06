package it.at.restfs.auth;

import java.util.Optional;
import java.util.UUID;

public interface AuthorizationChecker {	
	
	/*
	 * TODO if classes in construction method will be always the same ... make it ones !!?
	 */
	
	public enum Implementation {
		NO_AUTH(NoAuthorization.class, NoAuthorization.class),
		OAUTH2(OAuth2.class, OAuth2.class),
		MASTER_PWD(MasterPassword.class, MasterPassword.class),
		BASIC_AUTH(BasicAuth.class, BasicAuth.class);
		
		public Class<? extends AuthorizationChecker> c;
		public Class<? extends AuthorizationMaker> m;
		
		private Implementation(Class<? extends AuthorizationChecker> c, Class<? extends AuthorizationMaker> m) {
			this.c = c;
			this.m = m;
		}
	}		
	
	public boolean isTokenValid(UUID container, Optional<String> authorization);

}
