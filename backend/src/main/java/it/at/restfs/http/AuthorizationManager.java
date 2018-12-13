package it.at.restfs.http;

import java.util.UUID;
import com.google.inject.Inject;
import it.at.restfs.storage.Storage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AuthorizationManager {
    
    private final Storage storage;


    public boolean isTokenValidFor(String authorization, UUID container) {
        
        boolean exist = storage.exist(container);
        if (! exist) {
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
