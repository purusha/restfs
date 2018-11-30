package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.PathMatchers.*;
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
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.Rejection;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.LogEntry;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPListener {
    
    //XXX HTTP binding
    public static final String APP_NAME = "restfs";
    public static final String VERSION = "v1";
    public static final String HOST = "localhost";
    public static final int PORT = 8081;    
    
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
        
        final Flow<HttpRequest, HttpResponse, NotUsed> flow = createRoute().flow(system, materializer);
        this.bindAndHandle = http.bindAndHandle(flow, ConnectHttp.toHost(HOST, PORT), materializer);
    }

    //XXX call this please when application shutdown
    public void shutdown() {
        try {
            bindAndHandle.toCompletableFuture().get().unbind();
        } catch (InterruptedException | ExecutionException e) { }
    }

    private Route createRoute() {
        return
                
        logRequestResult(REQ, REJ, () -> 
            pathPrefix(segment(APP_NAME), () ->
                pathPrefix(segment(VERSION), () ->                                        
                    headerValueByName("Authorization", (String authorization) ->
                        headerValueByName("X-Container", (String container) ->
                            extractUri(uri ->
                                extractMethod(method ->
                                    parameter("op", (String operation) ->
                                        callHandler(UUID.fromString(container), authorization, uri, method, operation)
                                    )
                                )
                            )
                        )
                    )                                        
                )
            )
        );
    }
    
    private Route callHandler(UUID container, String authorization, Uri uri, HttpMethod method, String operation) {        
        LOGGER.debug("Http method is {}", method);
        
        if (! authManager.isTokenValidFor(authorization, container)) {
            throw new RuntimeException("token not valid"); //XXX client receive: HTTP/1.1 500 Internal Server Error
        }
                
        final Function<Request, Route> controller = mapping.get(method);
        
        if (Objects.isNull(controller)) {
            throw new RuntimeException("can't handle " + method); //XXX client receive: HTTP/1.1 500 Internal Server Error
        }
        
        return controller.apply(new Request(
            container, StringUtils.substringAfter(uri.getPathString(), APP_NAME + "/" + VERSION), operation
        ));
    }
    
    @Data
    @RequiredArgsConstructor    
    public class Request {
        final UUID container;
        final String path;
        final String operation;
    }
        
}