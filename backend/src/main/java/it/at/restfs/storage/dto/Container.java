package it.at.restfs.storage.dto;

import java.util.UUID;

import lombok.Data;

/*
 	this object is created for internal purpose
 	will never be exposed out !!?
 */

@Data
public class Container {
	
	/*
		add a property for enable/disabled Container instance
		this field MUST BE checked only ones ... when HTTPEndpoint will be called
		all call received on disabled container will respond forbidden (403)
		
		- un container potrà essere disabilitato/abilitato solo dagli endpoint di Admin
		
	 */
	
    private String name;
    private UUID id;
    
    private boolean statsEnabled;
    private boolean webHookEnabled;
    private String storage;
    private String authorization;
    
}
