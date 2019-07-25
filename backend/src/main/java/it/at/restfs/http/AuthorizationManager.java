package it.at.restfs.http;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.Storage;

public class AuthorizationManager {
    
    private final ContainerRepository cRepo;
    private final List<Storage> storages;
    
    @Inject
    public AuthorizationManager(Injector injector, ContainerRepository cRepo) {
    	this.cRepo = cRepo;
    	
    	this.storages = Arrays
    		.stream(Storage.Implementation.values())
	    	.map(i -> injector.getInstance(Key.get(Storage.class, Names.named(i.key))))
	    	.collect(Collectors.toList());
    }


    public boolean isTokenValidFor(String authorization, UUID container) {
        
        if ( !existsSomeWhere(container) || !cRepo.exist(container) ) {
            return false;
        }
                
        /*
            
            TODO:
            
            1) check if 'authorization' is valid
            
            2) check if 'authorization' is associated to 'container'
    
         */
        
        return true;
    }
    
    private boolean existsSomeWhere(UUID container) {
    	return storages.stream().anyMatch(s -> s.exist(container));
    }

}
