package it.at.restfs.auth;

import java.util.Optional;
import java.util.UUID;

public interface AuthorizationChecker {	
	
	public enum Implementation {
		//XXX add BasicAuth method !!?
		NO_AUTH("noAuth", NoAuthorization.class),
		OAUTH2("oAuth2", OAuth2.class),
		MASTER_PWD("masterPwd", MasterPassword.class);
		
		public String key; 
		public Class<? extends AuthorizationChecker> implClazz;
		
		private Implementation(String k, Class<? extends AuthorizationChecker> i) {
			this.key = k;
			this.implClazz = i;
		}
	}		
	
	public boolean isTokenValid(UUID container, Optional<String> authorization);

}
