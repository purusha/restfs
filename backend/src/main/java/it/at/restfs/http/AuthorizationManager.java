package it.at.restfs.http;

import java.util.UUID;
import com.google.inject.Inject;

public class AuthorizationManager {
    
    @Inject
    public AuthorizationManager() {
        
    }

    public boolean isTokenValidFor(String authorization, UUID container) {
        /*
            
            TODO:
            
            1) check if 'authorization' is valid
            
            2) check if 'authorization' is associated to 'container'
    
         */
        
        return true;
    }

}
