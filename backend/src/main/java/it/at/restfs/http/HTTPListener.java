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
import static akka.http.javadsl.server.Directives.route;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.PathMatchers.segment;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Rejection;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.LogEntry;
import akka.stream.ActorMaterializer;
import it.at.restfs.storage.ContainerRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class HTTPListener {
        
    //XXX HTTP binding ... please use conf file for this
    public static final String APP_NAME = "restfs";
    public static final String VERSION = "v1";
    public static final String HOST = "localhost";
    public static final int PORT = 8081;    
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
    private final Map<HttpMethod, Function<Request, Route>> mapping;
    private final AuthorizationManager authManager;     
    private final ExceptionHandler handler;
    private final Filter filter;
    private final ContainerRepository cRepo;
    
    @Inject
    public HTTPListener(
        Http http, 
        ActorSystem system, 
        ActorMaterializer materializer,
        Map<HttpMethod, Function<Request, Route>> mapping,
        AuthorizationManager authManager,
        ExceptionHandler handler,
        Filter filter,
        ContainerRepository cRepo
    ) {
        this.mapping = mapping;
        this.authManager = authManager;
        this.handler = handler;
        this.filter = filter;
        this.cRepo = cRepo;
        
        LOGGER.info("\n");
        LOGGER.info("Expose following endpoint");
        LOGGER.info("http://" + HOST + ":" + PORT + "/" + APP_NAME + "/" + VERSION + "/...");
        LOGGER.info("\n");
                
        this.bindAndHandle = http.bindAndHandle(
            createRoute().flow(system, materializer), ConnectHttp.toHost(HOST, PORT), materializer
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
                                                    handler(UUID.fromString(container), authorization, uri, method, operation)
                                                )
                                            )
                                        ),
                                        pathPrefix(segment("stats"), () ->
                                            get(() ->                                            
                                                stats(UUID.fromString(container), authorization)
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

    //XXX this method should be moved into a Controller ?
    private Route stats(UUID container, String authorization) {        
        if (! authManager.isTokenValidFor(authorization, container)) {
            return complete(StatusCodes.FORBIDDEN);
        }
        
        return complete(
            StatusCodes.OK, 
            cRepo.load(container).getStatistics(), 
            Jackson.<Map<Integer, Long>>marshaller()
        );
    }
    
    private Route handler(UUID container, String authorization, Uri uri, HttpMethod method, String operation) {        
        if (! authManager.isTokenValidFor(authorization, container)) {
            return complete(StatusCodes.FORBIDDEN);
        }
        
        return mapping
            .getOrDefault(method, (Request request) -> complete(StatusCodes.METHOD_NOT_ALLOWED))
            .apply(new Request(container, getPathString(uri) , operation));
    }
    
    //XXX this method should be moved into a new Class ?
    @SneakyThrows
    public static String getPathString(Uri uri) {
        final String substringAfter = StringUtils.substringAfter(uri.getPathString(), APP_NAME + "/" + VERSION);
        final String decode = java.net.URLDecoder.decode(substringAfter, "UTF-8");
        
        return decode;
    }
    
    @Data
    @RequiredArgsConstructor    
    static public class Request {
        final UUID container;
        final String path;
        final String operation;
    }
        
}