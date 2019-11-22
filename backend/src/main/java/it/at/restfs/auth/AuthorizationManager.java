package it.at.restfs.auth;

import java.util.Optional;
import java.util.UUID;

import com.google.inject.Inject;

import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.dto.Container;

public class AuthorizationManager {
    
    private final ContainerRepository cRepo;
	private final AuthorizationResolver authResolver;    
    
    @Inject
    public AuthorizationManager(ContainerRepository cRepo, AuthorizationResolver authResolver) {
    	this.cRepo = cRepo;
		this.authResolver = authResolver;
    }

    public boolean isTokenValidFor(Optional<String> authorization, UUID container) {        
        if ( !cRepo.exist(container) ) {
            return false;
        }
                
        return authResolver.getChecker(container).isTokenValid(container, authorization);
    }
    
	public String generateTokenFor(Container c) {
		return authResolver.getMaker(c.getId()).creteToken(c);
	}

}
