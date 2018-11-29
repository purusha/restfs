package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.extractUri;
import static akka.http.javadsl.server.Directives.headerValueByName;
import static akka.http.javadsl.server.Directives.logRequestResult;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.PathMatchers.segment;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import com.google.inject.Inject;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.Rejection;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.LogEntry;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPListener {
    
    //http binding
    public static final String APP_NAME = "restfs";
    public static final String VERSION = "v1";
    public static final String HOST = "localhost";
    public static final int PORT = 8081;    
    
    private final CompletionStage<ServerBinding> bindAndHandle;
    
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
            
    
    @Inject
    public HTTPListener(
        Http http, 
        ActorSystem system, 
        ActorMaterializer materializer
    ) {
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
                                handler(UUID.fromString(container), authorization, uri)
                            )
                        )
                    )                                        
                )
            )
        );
    }
    
    private Route handler(UUID container, String authorization, Uri uri) {        
        /*
            
            TODO:
            
            1) check if 'authorization' is valid
            
            2) check if 'authorization' is associated to 'container'
            
            3) run handler for uri
 
         */
        
        return complete(
            String.format("Authorization %s\nContainer %s\nUri %s\n", container, authorization, uri)
        );        
    }
    
}