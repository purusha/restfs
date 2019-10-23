package it.at.restfs.auth;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.Storage;
import it.at.restfs.storage.dto.Container;

public class AuthorizationManager {
    
    private final ContainerRepository cRepo;
    private final List<Storage> storages;
	private final AuthorizationResolver authResolver;    
    
    @Inject
    public AuthorizationManager(Injector injector, ContainerRepository cRepo, AuthorizationResolver authResolver) {
    	this.cRepo = cRepo;
		this.authResolver = authResolver;
    	
    	this.storages = Arrays
    		.stream(Storage.Implementation.values())
	    	.map(i -> injector.getInstance(Key.get(Storage.class, Names.named(i.key))))
	    	.collect(Collectors.toList());
    }

    public boolean isTokenValidFor(Optional<String> authorization, UUID container) {        
        if ( !existsSomewhere(container) || !cRepo.exist(container) ) {
            return false;
        }
                
        return authResolver.getChecker(container).isTokenValid(container, authorization);
    }
    
    private boolean existsSomewhere(UUID container) {
    	return storages.stream().anyMatch(s -> s.exist(container));
    }

	public String generateTokenFor(Container c) {
		return authResolver.getMaker(c.getId()).creteToken(c);
	}

}
