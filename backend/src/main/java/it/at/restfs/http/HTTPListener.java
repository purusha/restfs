package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.PathMatchers.segment;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.google.inject.Inject;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Rejection;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.LogEntry;
import akka.stream.ActorMaterializer;
import it.at.restfs.storage.ResouceNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPListener {
    
    final ExceptionHandler handler = ExceptionHandler.newBuilder()
        .match(ResouceNotFoundException.class, x ->
            complete(StatusCodes.NOT_FOUND, x.getMessage())
        )
        .build();    
    
    //XXX HTTP binding
    public static final String APP_NAME = "restfs";
    public static final String VERSION = "v1";
    public static final String HOST = "localhost";
    public static final int PORT = 8081;    
    public static final String X_CONTAINER = "X-Container";
    public static final String AUTHORIZATION = "Authorization";

    
    /*
     
        XXX LogEntry.create level mapping !!? please try to find out another to do the same stuff (without using magical 3 number)
      
        1 -> ERROR
        2 -> WARN
        3 -> INFO
        4 ...
        
     */
    
    private final static BiFunction<HttpRequest, HttpResponse, LogEntry> REQ =
        (request, response) ->             
            LogEntry.create(
                request.method().name() + ":" + response.status().intValue() +  " " + request.getUri().getPathString(), 
                3
            );

    private final static BiFunction<HttpRequest, List<Rejection>, LogEntry> REJ =
        (request, rejections) ->             
            LogEntry.create(
                rejections
                    .stream()
                    .map(Rejection::toString)
                    .collect(Collectors.joining(", ")),
                3
            );

    private final CompletionStage<ServerBinding> bindAndHandle;
    private final Map<HttpMethod, Function<Request, Route>> mapping;
    private final AuthorizationManager authManager;            
    
    @Inject
    public HTTPListener(
        Http http, 
        ActorSystem system, 
        ActorMaterializer materializer,
        Map<HttpMethod, Function<Request, Route>> mapping,
        AuthorizationManager authManager
    ) {
        this.mapping = mapping;
        this.authManager = authManager;
        
        LOGGER.info("\n");
        LOGGER.info("Expose following PRIVATE http endpoint");
        LOGGER.info("[GET] http://" + HOST + ":" + PORT + "/" + APP_NAME + "/" + VERSION + "/...");
        LOGGER.info("\n");
        LOGGER.info("Expose following PUBLIC http endpoint");
        LOGGER.info("[GET] http://" + HOST + ":" + PORT + "/" + APP_NAME + "/" + VERSION + "/...");
        LOGGER.info("\n");
                
        this.bindAndHandle = http.bindAndHandle(
            createRoute().flow(system, materializer), 
            ConnectHttp.toHost(HOST, PORT), 
            materializer
        );
    }

    //XXX call this please when application shutdown
    public void shutdown() {
        try {
            bindAndHandle.toCompletableFuture().get().unbind();
        } catch (InterruptedException | ExecutionException e) { }
    }

    private Route createRoute() {
        return handleExceptions(handler, () ->                             
            logRequestResult(REQ, REJ, () -> 
                pathPrefix(segment(APP_NAME), () ->
                    pathPrefix(segment(VERSION), () ->
                        headerValueByName("Accept", (String accept) -> {
    
                            if (! StringUtils.equals(accept, "application/json")) {
                                return complete(StatusCodes.BAD_REQUEST, "add header \"Accept: application/json\"");
                            }
                        
                            return headerValueByName(AUTHORIZATION, (String authorization) ->
                                headerValueByName(X_CONTAINER, (String container) ->                            
                                    extractUri(uri ->
                                        extractMethod(method ->
                                            parameter("op", (String operation) ->
                                                callHandler(UUID.fromString(container), authorization, uri, method, operation)
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
    
    private Route callHandler(UUID container, String authorization, Uri uri, HttpMethod method, String operation) {        
        if (! authManager.isTokenValidFor(authorization, container)) {
            return complete(StatusCodes.FORBIDDEN);
        }

        LOGGER.debug("Http method is {}", method);
        final Function<Request, Route> controller = mapping.get(method);
        
        if (Objects.isNull(controller)) {
            return complete(StatusCodes.METHOD_NOT_ALLOWED);
        }
        
        return controller.apply(new Request(
            container, StringUtils.substringAfter(uri.getPathString(), APP_NAME + "/" + VERSION), operation
        ));
    }
    
    @Data
    @RequiredArgsConstructor    
    static public class Request {
        final UUID container;
        final String path;
        final String operation;
    }
        
}