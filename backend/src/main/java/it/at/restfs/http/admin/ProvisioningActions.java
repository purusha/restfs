package it.at.restfs.http.admin;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import it.at.restfs.auth.AuthorizationChecker;
import it.at.restfs.auth.AuthorizationConfigHandler;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.RootFileSystem;
import it.at.restfs.storage.Storage;
import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.Container;

public class ProvisioningActions {
	
	private static final RandomStringGenerator GENERATOR = new RandomStringGenerator.Builder().withinRange('a', 'z').build();	

    private final ContainerRepository cRepo;
	private final RootFileSystem rfs;
	private final AuthorizationConfigHandler configResolver;
    
    @Inject
    public ProvisioningActions(
        ContainerRepository cRepo,
        RootFileSystem rfs,
        AuthorizationConfigHandler configResolver
    ) {
        this.cRepo = cRepo;
		this.rfs = rfs;
		this.configResolver = configResolver;
    }

    public Container updateContainer(Map<String, String> params) {
    	return null;
    }

    public Container createContainer(Map<String, String> params) {
    	
    	final String name = getOrDefault(params.get("name"), GENERATOR.generate(12));
        final UUID id = UUID.fromString(getOrDefault(params.get("id"), UUID.randomUUID().toString()));
        final Boolean statsEnabled = Boolean.valueOf(getOrDefault(params.get("statsEnabled"), Boolean.FALSE.toString()));
        final Boolean webHookEnabled = Boolean.valueOf(getOrDefault(params.get("webHookEnabled"), Boolean.FALSE.toString()));        
        final String storage = getOrDefault(params.get("storage"), Storage.Implementation.FS.key);
        final String authorization = getOrDefault(params.get("authorization"), AuthorizationChecker.Implementation.NO_AUTH.name());        
        
        final Container container = new Container();
        container.setName(name);
        container.setId(id); //XXX check for not existing container with id when params.get("id") is filled
        container.setStatsEnabled(statsEnabled);
        container.setWebHookEnabled(webHookEnabled);
        container.setStorage(storage);
        container.setAuthorization(authorization);
                
        //XXX do u remember Open-Close principle ???
        switch(AuthorizationChecker.Implementation.valueOf(authorization)) {
        
			case MASTER_PWD: {
				
				final String pwd = params.get(AuthorizationChecker.Implementation.MASTER_PWD.name());
				
				if (StringUtils.isBlank(pwd)) {
					throw new RuntimeException("mandatory field not resolved for container: " + id);
				}
				
				configResolver.save(
					container, 
					Collections.singletonMap(AuthorizationChecker.Implementation.MASTER_PWD.name(), pwd)
				);
				
			}break;
			
			case BASIC_AUTH: {
				
				final String user = params.get("user");
				final String pwd = params.get("pwd");
				
				if (StringUtils.isBlank(user) || StringUtils.isBlank(pwd)) {
					throw new RuntimeException("mandatory field not resolved for container: " + id);
				}
				
				final Map<String, String> data = Maps.newHashMap();
				data.put("user", user);
				data.put("pwd", pwd);
				
				configResolver.save(
					container, 
					data
				);
				
			}break;
			
			case NO_AUTH:
				break;
				
			case OAUTH2:
				break;
				
			default:
				break;    
				
        }
        
        cRepo.save(container);     
        
        //XXX call this only if container storage is FS
        rfs.containerPath(id, AbsolutePath.EMPTY).toFile().mkdir();
        
        return container;
    }
    
    private String getOrDefault(String value, String defaultValue) {
        return StringUtils.isBlank(value) || "null".equals(value) ? defaultValue : value;
    }
    
}
