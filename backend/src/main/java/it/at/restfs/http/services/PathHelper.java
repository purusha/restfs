package it.at.restfs.http.services;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import akka.http.javadsl.model.Uri;
import it.at.restfs.storage.dto.AbsolutePath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

public class PathHelper {
    
    public static final String APP_NAME = "restfs";
    public static final String VERSION = "v1";    

    @SneakyThrows
    public static AbsolutePath getPathString(Uri uri) {
        final String substringAfter = StringUtils.substringAfter(uri.getPathString(), APP_NAME + "/" + VERSION);
        final String decode = java.net.URLDecoder.decode(substringAfter, "UTF-8");
        
        return AbsolutePath.of(decode);
    }
    
    //XXX if /stats endpoint is called ... operation is NULL (like all managements operation) 
    public static Request build(UUID container, AbsolutePath uri, String operation) {
		return new Request(container, uri , operation);
	}
    
    public static ContainerAuth build(String container, Optional<String> authorization) {
    	return new ContainerAuth(UUID.fromString(container), authorization); //XXX what happens if container is not an UUID instance ?
    }
        
	@Getter
	@RequiredArgsConstructor	
	public static class ContainerAuth {		
		private final UUID container;
		private final Optional<String> authorization;		
	}
    
	@Getter 
	@RequiredArgsConstructor	
    public static class Request {
        private final UUID container; //XXX is usefull only for future development ??
        private final AbsolutePath path;
        private final String operation; //should be Optional<String>
    }	

	@Getter 
	public static class RequestView {		
        private final String path;
        private final String operation;
        
        @JsonCreator
        public RequestView(
    		@JsonProperty("path") String path,
    		@JsonProperty("operation") String operation
		) {
            this.path = path;
            this.operation = operation;        	
        }        			
	}
}
