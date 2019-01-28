package it.at.restfs.http;

import java.util.UUID;
import com.google.inject.Inject;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.Storage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AuthorizationManager {
    
    private final Storage storage;
    private final ContainerRepository cRepo;


    public boolean isTokenValidFor(String authorization, UUID container) {
        
        if ( !storage.exist(container) || !cRepo.exist(container) ) {
            return false;
        }
                
        /*
            
            TODO:
            
            1) check if 'authorization' is valid
            
            2) check if 'authorization' is associated to 'container'
    
         */
        
        return true;
    }

}
