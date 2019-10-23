package it.at.restfs.auth;

import it.at.restfs.storage.dto.Container;

public interface AuthorizationMaker {	
	
	public String creteToken(Container container);

}
