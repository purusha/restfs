package it.at.restfs.http;

import static akka.event.Logging.InfoLevel;
import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.extractMethod;
import static akka.http.javadsl.server.Directives.extractUri;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.handleExceptions;
import static akka.http.javadsl.server.Directives.headerValueByName;
import static akka.http.javadsl.server.Directives.logRequestResult;
import static akka.http.javadsl.server.Directives.parameter;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.route;
import static akka.http.javadsl.server.PathMatchers.segment;
import static it.at.restfs.http.PathResolver.APP_NAME;
import static it.at.restfs.http.PathResolver.VERSION;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import akka.stream.ActorMaterializer;
import lombok.Getter;
import lombok.Setter;
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
        ActorMaterializer materializer,
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
                        
                            return headerValueByName(AUTHORIZATION, (String authorization) ->
                                headerValueByName(X_CONTAINER, (String container) ->    
                                    route(
                                        
                                        parameter(OP, (String operation) ->
                                            extractUri(uri ->
                                                extractMethod(method ->                                                
                                                	runner.handler(UUID.fromString(container), authorization, uri, method, operation)
                                                )
                                            )
                                        ),
                                        pathPrefix(segment("stats"), () ->
                                            get(() ->                                            
                                            	runner.stats(UUID.fromString(container), authorization)
                                            )
                                        ),
                                        pathPrefix(segment("last"), () ->
                                            get(() ->                                            
                                            	runner.last(UUID.fromString(container), authorization)
                                            )
                                        )
                                                                            
                                    )
                                )
                            );
                            
                        })                                        
                    )
                )
            )
        );
    }

    
    @Getter 
    @Setter
    static public class Request {
        private final UUID container;
        private final String path;
        private final String operation;
        
        @JsonCreator
        public Request(
            @JsonProperty("container") UUID container, 
            @JsonProperty("path") String path, 
            @JsonProperty("operation") String operation
        ) {
            this.container = container;
            this.path = path;
            this.operation = operation;
        }
    }
        
}