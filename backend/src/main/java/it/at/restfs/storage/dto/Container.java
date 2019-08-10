package it.at.restfs.storage.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class Container {

    private String name;
    
    private UUID id;
        
    private boolean statsEnabled = false;
    
    private boolean webHookEnabled = false;
    
}
