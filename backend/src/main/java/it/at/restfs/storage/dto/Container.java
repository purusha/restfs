package it.at.restfs.storage.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.at.restfs.auth.AuthorizationChecker;
import it.at.restfs.storage.Storage;
import lombok.Data;

@Data
public class Container {

    private String name;
    
    private UUID id;
    
    /*
    	add a property for enable/disabled Container instance
     */
        
    private boolean statsEnabled = false;
    
    private boolean webHookEnabled = false;
    
    @JsonIgnore
    public String getStorage() {
    	return Storage.Implementation.FS.key;
    }
    
    @JsonIgnore
    public String getAuthorization() {
    	return AuthorizationChecker.Implementation.NO_AUTH.key;
    }    
    
}
