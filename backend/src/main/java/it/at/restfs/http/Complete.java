package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.*;

import java.io.File;
import java.nio.file.Path;

import akka.http.javadsl.model.ContentType;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.MediaType.Binary;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.Route;

/*
    akka Facade complete* methods
*/
public class Complete {
    private static final String ABSOLUTE_PATH = new File("").getAbsolutePath();
    
    public static final Route NOT_FOUND_ROUTE = complete(StatusCodes.NOT_FOUND);
    
    public static Route textHtml(String payload) {
       return complete(HttpEntities.create(
           ContentTypes.TEXT_HTML_UTF8, payload
       ));
    }        

    public static Route json(String payload) {
        return complete(HttpEntities.create(
            ContentTypes.APPLICATION_JSON, payload
        ));
    }        
    
    public static Route internalError() {
       return complete(StatusCodes.INTERNAL_SERVER_ERROR);
    }

    public static Route internalError(Exception e) {
    	return complete(StatusCodes.INTERNAL_SERVER_ERROR, e.getMessage());
    }
    
    public static Route noContent() {
        return complete(StatusCodes.NO_CONTENT);
    }

    public static Route withType(ContentType type, Uri uri) {
        return complete(HttpEntities.create(
            type, buildPath(uri)
        ));        
    }
    
    public static Route withType(Binary type, Uri uri) {
        return withType(ContentTypes.create(type), uri);
    }
    
    public static Path buildPath(Uri uri) {
        return new File(ABSOLUTE_PATH + uri.path()).toPath();
    }    
    
    public static String uriResolver(Uri uri) {
        return String.format("%s://%s:%d/", uri.getScheme(), uri.getHost(), uri.getPort());
    }
    
//    public static Route redirectToLoginPage() {
//        return redirect(ApplicationContext.loginContextPath());
//    }

    public static Route redirect(Uri uri) {
        return akka.http.javadsl.server.Directives.redirect(uri, StatusCodes.MOVED_PERMANENTLY);
    }
    
    public static Route simple(String payload) {
        return complete(payload);
    }
    
}
