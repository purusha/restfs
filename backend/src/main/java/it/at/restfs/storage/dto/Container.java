package it.at.restfs.storage.dto;

import java.util.UUID;

import it.at.restfs.storage.Storage;
import lombok.Data;

@Data
public class Container {

    private String name;
    
    private UUID id;
        
    private boolean statsEnabled = false;
    
    private boolean webHookEnabled = false;
    
    public Storage.Implementation getStorage() {
    	return Storage.Implementation.FS;
    }
    
}
