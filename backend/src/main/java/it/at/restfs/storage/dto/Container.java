package it.at.restfs.storage.dto;

import java.util.HashMap;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import it.at.restfs.auth.AuthorizationChecker;
import it.at.restfs.storage.Storage;
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
    
    private boolean statsEnabled = false;
    private boolean webHookEnabled = false;
    private String storage = Storage.Implementation.FS.key;
    private String authorization = AuthorizationChecker.Implementation.MASTER_PWD.key;
        
    @JsonIgnore
	public Config getAuthorizationConfig() {
    	HashMap<String, String> data = Maps.newHashMap();
    	data.put("masterPwd", "");
    	
		return ConfigFactory.parseMap(data);
	}    
    
}
