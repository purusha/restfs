package it.at.restfs.http;

import static akka.event.Logging.InfoLevel;
import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.handleExceptions;
import static akka.http.javadsl.server.Directives.headerValueByName;
import static akka.http.javadsl.server.Directives.logRequestResult;
import static akka.http.javadsl.server.Directives.route;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
import it.at.restfs.storage.ContainerRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class AdminHTTPListener {
        
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

    private final CompletionStage<ServerBinding> bindAndHandle;
    private final Filter filter;
    private final ExceptionHandler handler;
    
    @Inject
    public AdminHTTPListener(
        Http http, 
        ActorSystem system, 
        ActorMaterializer materializer,
        AuthorizationManager authManager,
        ExceptionHandler handler,
        Filter filter,
        ContainerRepository cRepo
    ) {
        this.handler = handler;
        this.filter = filter;
        
        LOGGER.info("\n");
        LOGGER.info("Expose following Admin endpoint");
        LOGGER.info("http://" + HOST + ":" + PORT + "/...");
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
                headerValueByName("Accept", (String accept) -> {
                    
                    if (! StringUtils.equals(accept, "application/json")) {
                        return complete(StatusCodes.BAD_REQUEST, "add header \"Accept: application/json\"");
                    }
                
                    return route(
                        
                    );
                    
                })
            )
        );
    }
    
    /*

        how are protected this endpoint ?
        
        for how many size/time is available a new container ? (100MB or 48hours)
        
         

     */
        
}