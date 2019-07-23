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
import static it.at.restfs.http.PathResolver.getPathString;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

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
import it.at.restfs.event.Event;
import it.at.restfs.http.PerRequestContext.Factory;
import it.at.restfs.storage.ContainerRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
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
    private final Map<HttpMethod, Controller> mapping;
    private final AuthorizationManager authManager;     
    private final ExceptionHandler handler;
    private final Filter filter;
    private final ContainerRepository cRepo;
	private final Factory factory;
    
    @Inject
    public HTTPListener(
        Config config,
        Http http, 
        ActorSystem system, 
        ActorMaterializer materializer,
        Map<HttpMethod, Controller> mapping,
        AuthorizationManager authManager,
        ExceptionHandler handler,
        Filter filter,
        ContainerRepository cRepo,
        PerRequestContext.Factory factory
    ) {
        this.mapping = mapping;
        this.authManager = authManager;
        this.handler = handler;
        this.filter = filter;
        this.cRepo = cRepo;
		this.factory = factory;
        
        final String host = config.getString("restfs.http.public.interface");
        final Integer port = config.getInt("restfs.http.public.port");
        
        LOGGER.info("\n");
        LOGGER.info("Expose following endpoint");
        LOGGER.info("http://" + host + ":" + port + "/" + APP_NAME + "/" + VERSION + "/...");
        LOGGER.info("\n");
                
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
                                                    handler(UUID.fromString(container), authorization, uri, method, operation)
                                                )
                                            )
                                        ),
                                        pathPrefix(segment("stats"), () ->
                                            get(() ->                                            
                                                stats(UUID.fromString(container), authorization)
                                            )
                                        ),
                                        pathPrefix(segment("last"), () ->
                                            get(() ->                                            
                                                last(UUID.fromString(container), authorization)
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
    //XXX and should be executed in a Future ?
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

    //XXX this method should be moved into a Controller ?
    //XXX and should be executed in a Future ?    
    private Route last(UUID container, String authorization) {        
        if (! authManager.isTokenValidFor(authorization, container)) {
            return complete(StatusCodes.FORBIDDEN);
        }
        
        return complete(
            StatusCodes.OK, 
            cRepo.getCalls(container), 
            Jackson.<List<Event>>marshaller()
        );
    }
        
	@SneakyThrows(Throwable.class)
    private Route handler(UUID container, String authorization, Uri uri, HttpMethod method, String operation) {        
        if (! authManager.isTokenValidFor(authorization, container)) {
            return complete(StatusCodes.FORBIDDEN);
        }
        
        final Controller controller = mapping.get(method);
        
        if (Objects.isNull(controller)) {
        	complete(StatusCodes.METHOD_NOT_ALLOWED);
        }
        
        final Request request = new Request(container, getPathString(uri) , operation);
        
        try {
        	final Field field = resolveField(controller.getClass());        	        	        	
        	field.set(controller, factory.create(request));
        	
            return (Route) controller.getClass().getDeclaredMethod(request.getOperation().toLowerCase(), Request.class).invoke(controller, request);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
            throw e;
        } catch (InvocationTargetException e) { //XXX important ... because we use Reflection !!!?
            throw e.getCause();
        }
    }

	//XXX per queste cose ci sarebbe la pena di morte !!!?
	@SuppressWarnings("unchecked")
	private Field resolveField(Class<? extends Controller> class1) {
		//final Field field = controller.getClass().getDeclaredField("x"); 
		
		final Field field = ReflectionUtils.getFields(class1, new Predicate<Field>() {
			@Override
			public boolean apply(Field field) {
				return field.getType().equals(PerRequestContext.class);
			}
		}).iterator().next();
		
		field.setAccessible(true);
		
		return field;
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