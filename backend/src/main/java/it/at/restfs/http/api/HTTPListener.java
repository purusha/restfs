package it.at.restfs.http.api;

import static akka.event.Logging.InfoLevel;
import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.extractMethod;
import static akka.http.javadsl.server.Directives.extractUri;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.handleExceptions;
import static akka.http.javadsl.server.Directives.headerValueByName;
import static akka.http.javadsl.server.Directives.logRequestResult;
import static akka.http.javadsl.server.Directives.optionalHeaderValueByName;
import static akka.http.javadsl.server.Directives.parameter;
import static akka.http.javadsl.server.Directives.pathEndOrSingleSlash;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.post;
import static akka.http.javadsl.server.Directives.route;
import static akka.http.javadsl.server.PathMatchers.segment;
import static it.at.restfs.http.services.PathHelper.APP_NAME;
import static it.at.restfs.http.services.PathHelper.VERSION;
import static it.at.restfs.http.services.PathHelper.build;

import java.util.List;
import java.util.Optional;
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
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Rejection;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.LogEntry;
import akka.stream.Materializer;
import it.at.restfs.http.services.Filter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class HTTPListener {
        
    public static final String X_CONTAINER = "X-Container";
    public static final String AUTHORIZATION = "Authorization";
    public static final String OP = "op";
    
    private final static BiFunction<HttpRequest, List<Rejection>, LogEntry> REJ = (request, rejections) ->    
        LogEntry.create(
            rejections
                .stream()
                .map(Rejection::toString)
                .collect(Collectors.joining(", ")),
            InfoLevel() //was 3
        );

    private final CompletionStage<ServerBinding> bindAndHandle;
    private final ExceptionHandler handler;
    private final Filter filter;
	private final ControllerRunner runner;
    
    @Inject
    public HTTPListener(
        Config config,
        Http http, 
        ActorSystem system, 
        Materializer materializer,
        ExceptionHandler handler,
        Filter filter,
        ControllerRunner runner
    ) {
        this.handler = handler;
        this.filter = filter;
		this.runner = runner;
        
        final String host = config.getString("restfs.http.public.interface");
        final Integer port = config.getInt("restfs.http.public.port");
        
        LOGGER.info("");
        LOGGER.info("Expose following endpoint");
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
        return logRequestResult(filter, REJ, () ->
            handleExceptions(handler, () ->
                pathPrefix(segment(APP_NAME), () ->
                    pathPrefix(segment(VERSION), () ->
                        headerValueByName("Accept", (String accept) -> {
                            
                            if (! StringUtils.equals(accept, "application/json")) {
                                return complete(StatusCodes.BAD_REQUEST, "add header \"Accept: application/json\"");
                            }
                        
                            return optionalHeaderValueByName(AUTHORIZATION, (Optional<String> authorization) ->
                                headerValueByName(X_CONTAINER, (String container) ->    
                                    handler(container, authorization)
                                )
                            );
                            
                        })                                        
                    )
                )
            )
        );
    }
    
    private Route handler(String container, Optional<String> authorization) {
    	return route(
            
            parameter(OP, (String operation) ->
                extractUri(uri ->
                    extractMethod(method ->                                                
                    	runner.handler(build(container, authorization), uri, method, operation)
                    )
                )
            ),
            
            //management endpoints from here
            pathPrefix("stats", () -> 
            	pathEndOrSingleSlash(() ->
                    get(() ->                                            
                    	runner.stats(build(container, authorization))
                    )
                )
            ),
            pathPrefix("last", () ->
            	pathEndOrSingleSlash(() ->
                    get(() ->                                            
                    	runner.last(build(container, authorization))
                    )
                )
            ),
            pathPrefix("token", () ->
            	pathEndOrSingleSlash(() ->
                	post(() ->
                		runner.token(build(container, authorization))
                	)
            	)
            )
            
        );    	
    }
        
}