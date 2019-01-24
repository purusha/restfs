package it.at.restfs.http;

import static akka.event.Logging.InfoLevel;
import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.formFieldMap;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.handleExceptions;
import static akka.http.javadsl.server.Directives.headerValueByName;
import static akka.http.javadsl.server.Directives.logRequestResult;
import static akka.http.javadsl.server.Directives.pathEndOrSingleSlash;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.post;
import static akka.http.javadsl.server.Directives.route;
import static akka.http.javadsl.server.PathMatchers.segment;
import static akka.http.javadsl.server.PathMatchers.uuidSegment;
import static it.at.restfs.http.PathResolver.APP_NAME;
import static it.at.restfs.http.PathResolver.VERSION;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Rejection;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.LogEntry;
import akka.stream.ActorMaterializer;
import it.at.restfs.storage.Container;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.FileSystemStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class AdminHTTPListener {
    
    private static final org.apache.commons.text.RandomStringGenerator TEXT_BUILDER = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
    
    public static final String CONTAINERS = "containers";
        
    //XXX HTTP binding ... please use conf file for this
    public static final String HOST = "localhost";
    public static final int PORT = 8086;    
    
    private final static BiFunction<HttpRequest, List<Rejection>, LogEntry> REJ = (request, rejections) ->             
        LogEntry.create(
            rejections
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
    private final ContainerRepository cRepo;
    
    @Inject
    public AdminHTTPListener(
        Http http, 
        ActorSystem system, 
        ActorMaterializer materializer,
        AuthorizationManager authManager,
        ExceptionHandler handler,
        ContainerRepository cRepo
    ) {
        this.cRepo = cRepo;
        this.handler = handler;
        
        LOGGER.info("\n");
        LOGGER.info("Expose following Admin endpoint");
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
        return logRequestResult(REQ, REJ, () ->
            handleExceptions(handler, () ->
                route(
                    
                    //page complete
                    pathPrefix(segment(CONTAINERS), () ->
                        pathEndOrSingleSlash(() ->
                            get(() ->
                                route()
                            )
                        )
                    ),

                    //page complete
                    pathPrefix(segment(CONTAINERS), () ->
                        pathPrefix(uuidSegment(), (UUID containerId) ->
                            route()
                        )
                    ),                    
                    
                    //api
                    pathPrefix(segment(APP_NAME), () ->
                        pathPrefix(segment(VERSION), () ->                    
                            headerValueByName("Accept", (String accept) -> {
                                
                                if (! StringUtils.equals(accept, "application/json")) {
                                    return complete(StatusCodes.BAD_REQUEST, "add header \"Accept: application/json\"");
                                }
                            

                                return pathPrefix(segment(CONTAINERS), () ->
                                    post(() ->
                                        formFieldMap((Map<String, String> map) ->
                                            createContainer(map)
                                        )
                                    )
                                );
                            })
                        )
                    )                    
                    
                )
            )
        );
    }

    private Route createContainer(Map<String, String> map) {
        final String name = map.getOrDefault("name", TEXT_BUILDER.generate(12));
        final UUID id = UUID.fromString(map.getOrDefault("id", UUID.randomUUID().toString()));
        final Boolean statsEnabled = Boolean.valueOf(map.getOrDefault("statsEnabled", Boolean.TRUE.toString()));
        final Boolean webHookEnabled = Boolean.valueOf(map.getOrDefault("webHookEnabled", Boolean.TRUE.toString()));

        final Container container = new Container();
        container.setName(name);
        container.setId(id);
        container.setStatsEnabled(statsEnabled);
        container.setWebHookEnabled(webHookEnabled);
        
        cRepo.save(container);
        
        Paths.get(FileSystemStorage.ROOT + "/" + id).toFile().mkdir();

        return complete(
            StatusCodes.CREATED, 
            container, 
            Jackson.<Container>marshaller()
        );
    }
    
    /*

        how are protected this endpoints ?
        
        for how many size/time is available a new container ? (100MB or 48hours)
        
     */
        
}