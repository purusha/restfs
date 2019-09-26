package it.at.restfs.http;

import static akka.event.Logging.InfoLevel;
import static akka.http.javadsl.server.Directives.extractUri;
import static akka.http.javadsl.server.Directives.formFieldMap;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.handleExceptions;
import static akka.http.javadsl.server.Directives.logRequestResult;
import static akka.http.javadsl.server.Directives.pathEndOrSingleSlash;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.post;
import static akka.http.javadsl.server.Directives.redirect;
import static akka.http.javadsl.server.Directives.route;
import static akka.http.javadsl.server.PathMatchers.segment;
import static akka.http.javadsl.server.PathMatchers.uuidSegment;
import static it.at.restfs.http.services.Complete.internalError;
import static it.at.restfs.http.services.Complete.textHtml;
import static it.at.restfs.http.services.Complete.uriResolver;
import static it.at.restfs.http.services.PathHelper.APP_NAME;
import static it.at.restfs.http.services.PathHelper.VERSION;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.text.RandomStringGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Rejection;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.LogEntry;
import akka.stream.ActorMaterializer;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.RootFileSystem;
import it.at.restfs.storage.dto.Container;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class AdminHTTPListener {
    
    /*
    
        how are protected this endpoints ?
        
        for how many size/time is available a new container ? (100MB or 48hours)
        
     */
    
    private static final RandomStringGenerator GENERATOR = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
    
    public static final String CONTAINERS = "containers";
        
    private final static BiFunction<HttpRequest, List<Rejection>, LogEntry> REJ = (request, rejections) ->             
        LogEntry.create(
            "[ADMIN] " + rejections
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
    private final PageResolver pageResolver;
    private final String host;
    private final Integer port;
	private final RootFileSystem rfs;
    
    @Inject
    public AdminHTTPListener(
        Config config,
        Http http, 
        ActorSystem system, 
        ActorMaterializer materializer,
        ExceptionHandler handler,
        ContainerRepository cRepo,
        PageResolver pageResolver,
        RootFileSystem rfs
    ) {
        this.cRepo = cRepo;
        this.handler = handler;
        this.pageResolver = pageResolver;
		this.rfs = rfs;
        
        host = config.getString("restfs.http.admin.interface");
        port = config.getInt("restfs.http.admin.port");
        
        LOGGER.info("");
        LOGGER.info("Expose following Admin endpoint");
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
        return logRequestResult(REQ, REJ, () ->
            handleExceptions(handler, () ->
                extractUri(uri ->
                    route(
                        
                        //page complete
                        pathPrefix(segment(CONTAINERS), () ->
                            pathEndOrSingleSlash(() ->
                                get(() ->
                                    route(
                                        pageResolver.allContainer(uriResolver(uri))
                                    )
                                )
                            )
                        ),
    
                        //page complete
                        pathPrefix(segment(CONTAINERS), () ->
                            pathPrefix(uuidSegment(), (UUID containerId) ->
                                pathEndOrSingleSlash(() ->
                                    get(() ->
                                        route(
                                            pageResolver.getContainer(uriResolver(uri), containerId)
                                        )
                                    )
                                )
                            )
                        ),                    
                        
                        //api
                        pathPrefix(segment(APP_NAME), () ->
                            pathPrefix(segment(VERSION), () -> {                    
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
        final String name = getOrDefault(map.get("name"), GENERATOR.generate(12));
        final UUID id = UUID.fromString(getOrDefault(map.get("id"), UUID.randomUUID().toString()));
        final Boolean statsEnabled = Boolean.valueOf(getOrDefault(map.get("statsEnabled"), Boolean.FALSE.toString()));
        final Boolean webHookEnabled = Boolean.valueOf(getOrDefault(map.get("webHookEnabled"), Boolean.FALSE.toString()));

        final Container container = new Container();
        container.setName(name);
        container.setId(id);
        container.setStatsEnabled(statsEnabled);
        container.setWebHookEnabled(webHookEnabled);
        
        //XXX Provisioning actions: please extract a service !!?
        cRepo.save(container);        
        rfs.containerPath(id, "").toFile().mkdir();

        return redirect(Uri.create("http://" + host + ":" + port + "/" + CONTAINERS), StatusCodes.SEE_OTHER);
    }
        
    private String getOrDefault(String value, String defaultValue) {
        return StringUtils.isBlank(value) || "null".equals(value) ? defaultValue : value;
    }

    @Singleton    
    @AllArgsConstructor(onConstructor = @__({ @Inject }))
    static class PageResolver {
        
        private final TemplateResover resolver;
        private final ContainerRepository cRepo;
        
        public Route allContainer(String uri) {
            return inner(
                "all-container", uri,
                "containers", cRepo.findAll()
            );
        }
    
        public Route getContainer(String uri, UUID containerId) {
            return inner(
                "get-container", uri,
                "container", cRepo.load(containerId)
            );
        }
        
        private Route inner(String templateName, String uri, Object... kv) {
            final Map<String, Object> map = new HashMap<String, Object>();        
            for(int i = 0; i < kv.length; i += 2) {
                map.put(String.valueOf(kv[i]), kv[i+1]);
            }
            
            final Context context = context(uri, map);
            
            try {            
                return textHtml(resolver.get(templateName).apply(context));                    
            } catch (Exception e) {
                LOGGER.error("pageresolver inner error: ", e);
                return internalError();                    
            }
        }
        
        private Context context(String baseUri, Map<String, Object> data) {
            data.put("baseUri", baseUri); //this is used as base path of all static files (img, css and js)            

            return Context.newContext(data);        
        }                
    }
    
    @Singleton
    static class TemplateResover {
        
        private final static ObjectMapper MAPPER = new ObjectMapper();
        
        private final Map<String, Template> mapping = new HashMap<>();
        private final Handlebars handlebars = handlebars();
        
        public Template get(String name) throws IOException {
            //XXX NOT Recompile mode
            return mapping.computeIfAbsent(name, s -> {
                try {
                    return handlebars.compile(s);
                } catch (IOException e) {
                    LOGGER.error("can't compile template {}", name, e.getMessage());
                    return null;
                }
            });
            
            //XXX Recompile mode
//            return handlebars().compile(name); 
        }
        
        /*
            other helpers should be get from
            
            https://github.com/jknack/handlebars.java/tree/master/handlebars/src/main/java/com/github/jknack/handlebars/helper               
         */        
        private Handlebars handlebars() {
            final Handlebars handlebars = new Handlebars(new ClassPathTemplateLoader("/templates"));
            
            handlebars.registerHelper("size", new Helper<Object>() {
                @Override
                public CharSequence apply(Object ob, Options options) throws IOException {
                    if (ob instanceof Collection || ob instanceof Map) {
                        try {
                            final Method method = ob.getClass().getMethod("size");
                            method.setAccessible(true); //HashMap.KeySet is not public !!?
                            
                            return String.valueOf(method.invoke(ob));
                        } catch (Exception e) {
                            LOGGER.error("", e);
                            return StringUtils.EMPTY;
                        }
                    } else {
                        LOGGER.error("cannot access method 'size' on {} of class {}", ob, ob.getClass());
                        return StringUtils.EMPTY;
                    }
                }
            });
    
            handlebars.registerHelper("json", (o, options) -> MAPPER.writeValueAsString(o));
            
            handlebars.registerHelper("eq", new Helper<Object>() {
                @Override
                public Object apply(Object a, Options options) throws IOException {
                    final Object b = options.param(0, null);
                    boolean result = new EqualsBuilder().append(a, b).isEquals();
                    
                    if (options.tagType == TagType.SECTION) {
                        return result ? options.fn() : options.inverse();
                    } else {
                        return result ? options.hash("yes", true) : options.hash("no", false);                    
                    }                                 
                }            
            });
            
            handlebars.registerHelper("toS", (o, options) -> {
                if (o instanceof Collection) {
                    final Collection<?> c = (Collection<?>) o;
                    return c.stream().map(e -> e.toString()).collect(Collectors.joining(", "));
                }
                
                return StringUtils.EMPTY;
            });
    
            handlebars.registerHelper("substring", (o, options) -> {
                final String str = o.toString();
                final Integer start = options.param(0);
                final Integer end = options.param(1, str.length());
                
                return str.subSequence(start, end);
            });  
            
            handlebars.registerHelper("sum", (o, options) -> {
                final Integer a = Integer.valueOf(o.toString());
                final Integer b = options.param(0);
                return  a + b;
            });

            return handlebars;
        }            
    }    
        
}