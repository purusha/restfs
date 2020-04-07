package it.at.restfs.http.admin;

import static akka.event.Logging.InfoLevel;
import static akka.http.javadsl.server.Directives.extractUri;
import static akka.http.javadsl.server.Directives.formFieldMap;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.getFromResourceDirectory;
import static akka.http.javadsl.server.Directives.handleExceptions;
import static akka.http.javadsl.server.Directives.logRequestResult;
import static akka.http.javadsl.server.Directives.pathEndOrSingleSlash;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.post;
import static akka.http.javadsl.server.Directives.redirect;
import static akka.http.javadsl.server.Directives.route;
import static akka.http.javadsl.server.PathMatchers.segment;
import static akka.http.javadsl.server.PathMatchers.uuidSegment;
import static it.at.restfs.http.services.Complete.uriResolver;
import static it.at.restfs.http.services.PathHelper.APP_NAME;
import static it.at.restfs.http.services.PathHelper.VERSION;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Rejection;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.LogEntry;
import akka.stream.Materializer;
import it.at.restfs.storage.dto.Container;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class AdminHTTPListener {
	
    /*
        XXX how are protected this endpoints ?
     */
    
    public static final String CONTAINERS = "containers";
        
    private final static BiFunction<HttpRequest, List<Rejection>, LogEntry> REJ = (request, rejections) ->             
        LogEntry.create(
            "[ADMIN] " + rejections
                .stream()
                .map(Rejection::toString)
                .collect(Collectors.joining(", ")),
            InfoLevel() //was 3
        );
        
    private final static BiFunction<HttpRequest, HttpResponse, LogEntry> REQ = (request, response) ->    
        LogEntry.create(
            "[ADMIN] " + request.method().name() + ":" + response.status().intValue() +  " " + request.getUri().getPathString(), 
            InfoLevel() //was 3
        );    
        
    private final CompletionStage<ServerBinding> bindAndHandle;
    private final ExceptionHandler handler;
    private final PageResolver pageResolver;
    private final String host;
    private final Integer port;
    private final ProvisioningActions provisioning;
    
    @Inject
    public AdminHTTPListener(
        Config config,
        Http http, 
        ActorSystem system, 
        Materializer materializer,
        ExceptionHandler handler,
        PageResolver pageResolver,
        ProvisioningActions provisioning
    ) {
        this.handler = handler;
        this.pageResolver = pageResolver;
        this.provisioning = provisioning;
        
        host = config.getString("restfs.http.admin.interface");
        port = config.getInt("restfs.http.admin.port");
        
        LOGGER.info("");
        LOGGER.info("Expose following Admin endpoint");
        LOGGER.info("http://" + host + ":" + port + "/" + APP_NAME + "/" + VERSION + "/...");
        LOGGER.info("");
                                
        this.bindAndHandle = http.bindAndHandle(
            createRoute().flow(system, materializer), ConnectHttp.toHost(host, port), materializer
        );
    }

    //XXX call this please when application shutdown
    public void shutdown() {
        try {
            bindAndHandle.toCompletableFuture().get().unbind();
        } catch (InterruptedException | ExecutionException e) { }
    }

    private Route createRoute() {
        return logRequestResult(REQ, REJ, () ->
            handleExceptions(handler, () ->
                extractUri(uri ->
                    route(

                        //page complete
                        pathPrefix(segment("dashboard"), () ->
                            pathEndOrSingleSlash(() ->
                                get(() ->
                                    route(
                                        pageResolver.dashboard(uriResolver(uri))
                                    )
                                )
                            )
                        ),
                    		    
                        //page complete
                        pathPrefix(segment(CONTAINERS), () ->
                        	route(
                        			
                                pathEndOrSingleSlash(() ->
                                    get(() ->
                                        route(
                                            pageResolver.allContainer(uriResolver(uri))
                                        )
                                    )
                                ),
                        			
	                            pathPrefix(uuidSegment(), (UUID containerId) ->
	                                pathEndOrSingleSlash(() ->
	                                    get(() ->
	                                        route(
	                                            pageResolver.getContainer(uriResolver(uri), containerId)
	                                        )
	                                    )
	                                )
	                            ),

	                            pathPrefix(uuidSegment(), (UUID containerId) ->
	                            	pathPrefix(segment("edit"), () ->
		                                pathEndOrSingleSlash(() ->
		                                    get(() ->
		                                        route(
		                                            pageResolver.editContainer(uriResolver(uri), containerId)
		                                        )
		                                    )
		                                )
	                                )
	                            ),
	                            
	                            pathPrefix(segment("new"), () ->
		                            pathEndOrSingleSlash(() ->
			                            get(() ->
			                                route(
			                                    pageResolver.newContainer(uriResolver(uri))
			                                )
			                            )
	                        		)                            
	                            )
	                            
                            )
                        ),                                  	                        
                        
                        //static content
                        pathPrefix("css", () ->                        	
                    		get(() ->
                    			getFromResourceDirectory("css")
                    		)                        	
                        ),
                        
                        //static content
                        pathPrefix("js", () ->                        	
                    		get(() ->
                    			getFromResourceDirectory("js")
                    		)                        	
                        ),
                        
                        //static content
                        pathPrefix("assets", () ->                        	
                    		get(() ->
                    			getFromResourceDirectory("assets")
                    		)                        	
                        ),
                        
                        //api
                        pathPrefix(segment(APP_NAME), () ->
                            pathPrefix(segment(VERSION), () ->                     
                                pathPrefix(segment(CONTAINERS), () ->
                                	route(
	                                    post(() ->
	                                        formFieldMap((Map<String, String> map) -> {
	                                        	final Container c;
	                                        	
	                                        	if (map.containsKey("_method") && StringUtils.equals(map.get("_method"), "patch")) {
	                                        		c = provisioning.updateContainer(map);
	                                        	} else {
	                                        		c = provisioning.createContainer(map);
	                                        	}
	                                        	
	                                        	return redirect(Uri.create("http://" + host + ":" + port + "/" + CONTAINERS + "/" + c.getId()), StatusCodes.SEE_OTHER);
	                                        })
	                                    )
                                    )
                                )
                            )
                        )
                        
                    )
                )
            )
        );
    }
        
}
