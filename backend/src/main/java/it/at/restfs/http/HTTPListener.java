package it.at.restfs.http;

import static akka.event.Logging.InfoLevel;
import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.extractMethod;
import static akka.http.javadsl.server.Directives.extractUri;
import static akka.http.javadsl.server.Directives.handleExceptions;
import static akka.http.javadsl.server.Directives.headerValueByName;
import static akka.http.javadsl.server.Directives.logRequestResult;
import static akka.http.javadsl.server.Directives.parameter;
import static akka.http.javadsl.server.Directives.pathPrefix;
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
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
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
import it.at.restfs.actor.EventHandler;
import it.at.restfs.event.Event;
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
    public static final String X_CONTAINER = "X-Container";
    public static final String AUTHORIZATION = "Authorization";
    
    private final static BiFunction<HttpRequest, List<Rejection>, LogEntry> REJ = (request, rejections) ->             
        LogEntry.create(
            rejections
                .stream()
                .map(Rejection::toString)
                .collect(Collectors.joining(", ")),
            InfoLevel() //was 3
        );

    private final CompletionStage<ServerBinding> bindAndHandle;
    private final Map<HttpMethod, Function<Request, Route>> mapping;
    private final AuthorizationManager authManager;     
    private final ExceptionHandler handler;
    private final Filter filter;
    
    @Inject
    public HTTPListener(
        Http http, 
        ActorSystem system, 
        ActorMaterializer materializer,
        Map<HttpMethod, Function<Request, Route>> mapping,
        AuthorizationManager authManager,
        ExceptionHandler handler,
        Filter filter
    ) {
        this.mapping = mapping;
        this.authManager = authManager;
        this.handler = handler;
        this.filter = filter;
        
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

//        LOGGER.debug("Http method is {}", method);
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
    
    static public class Filter implements BiFunction<HttpRequest, HttpResponse, LogEntry> {        
        private final ActorSelection eventHandler;

        @Inject
        public Filter(ActorSystem system) {
            this.eventHandler = system.actorSelection("/user/" + EventHandler.ACTOR);
        }

        @Override
        public LogEntry apply(HttpRequest request, HttpResponse response) {
            final Request req = new Request(UUID.randomUUID(), "/booo", "operation"); //build this like in callHandler method above !!?
            final Event event = new Event(req, response.status());
            
            eventHandler.tell(event, ActorRef.noSender());
            
            return LogEntry.create(
                request.method().name() + ":" + response.status().intValue() +  " " + request.getUri().getPathString(), 
                InfoLevel() //was 3
            );
        }        
    }
        
}