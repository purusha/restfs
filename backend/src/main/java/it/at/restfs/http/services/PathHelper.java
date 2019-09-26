package it.at.restfs.http.services;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import akka.http.javadsl.model.Uri;
import it.at.restfs.http.ControllerRunner.ContainerAuth;
import it.at.restfs.http.HTTPListener.Request;
import lombok.SneakyThrows;

public class PathHelper {
    
    public static final String APP_NAME = "restfs";
    public static final String VERSION = "v1";    

    @SneakyThrows
    public static String getPathString(Uri uri) {
        final String substringAfter = StringUtils.substringAfter(uri.getPathString(), APP_NAME + "/" + VERSION);
        final String decode = java.net.URLDecoder.decode(substringAfter, "UTF-8");
        
        return decode;
    }
    
    //XXX if /stats (all managements) endpoint is called ... operation is NULL
    public static Request build(UUID container, String uri, String operation) {
		return new Request(container, uri , operation);
	}
    
    public static ContainerAuth buildCA(String container, String authorization) {
    	return new ContainerAuth(UUID.fromString(container), authorization); //XXX what happend if container is not an UUID instance ?
    }
    
}
