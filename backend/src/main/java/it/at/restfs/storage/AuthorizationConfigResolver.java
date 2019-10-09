package it.at.restfs.storage;

import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import it.at.restfs.storage.dto.Container;
import lombok.SneakyThrows;

public class AuthorizationConfigResolver {

	private final ObjectMapper mapper;
	private final RootFileSystem rfs;
	
	@Inject
    public AuthorizationConfigResolver(RootFileSystem rfs) {
    	this.rfs = rfs;
		this.mapper = new ObjectMapper(new YAMLFactory());
    }
	
	@SneakyThrows
	public void save(Container c, Map<String, String> data) {		
		mapper.writeValue(buildAuth(c), data);		
	}
	
	public Config get(Container c) {
		return ConfigFactory.parseFile(buildAuth(c));
	} 
	
	private File buildAuth(Container c) {
		return rfs.containerPath(c.getId(), "-" + c.getAuthorization()).toFile();
	}
}
