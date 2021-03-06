package it.at.restfs.auth;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import it.at.restfs.storage.RootFileSystem;
import it.at.restfs.storage.dto.Container;
import lombok.SneakyThrows;

public class AuthorizationConfigHandler {

	private final ObjectMapper mapper;
	private final RootFileSystem rfs;
	
	@Inject
    public AuthorizationConfigHandler(RootFileSystem rfs) {
    	this.rfs = rfs;
		this.mapper = new ObjectMapper(new YAMLFactory());
    }
	
	@SneakyThrows
	public void save(Container c, Map<String, String> data) {		
		mapper.writeValue(buildAuth(c), data);		
	}
	
	@SneakyThrows
	public Config get(Container c) {
		final Map<String, Object> content = mapper.<Map<String, Object>>readValue(
			buildAuth(c), new TypeReference<Map<String, Object>>() { }
		);
		
		return ConfigFactory.parseMap(content);
	} 
	
	private File buildAuth(Container c) {
		return rfs.pathOf("AUTH-", c.getId()).toFile();
	}
	
	//XXX this is a trick !!? because i know what call on Container instance
	@SneakyThrows
	public Config get(UUID container) {
		final Container c = new Container(); 
		c.setId(container);
		
		return get(c);
	}
}
