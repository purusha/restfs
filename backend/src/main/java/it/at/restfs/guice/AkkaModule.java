package it.at.restfs.guice;

import static akka.http.javadsl.server.Directives.complete;

import java.nio.file.FileAlreadyExistsException;
import java.util.concurrent.CompletionException;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.ExceptionHandler;
import akka.stream.ActorMaterializer;
import it.at.restfs.event.EventRepository;
import it.at.restfs.event.ShortTimeInMemory;
import it.at.restfs.http.Controller;
import it.at.restfs.http.DeleteController;
import it.at.restfs.http.Filter;
import it.at.restfs.http.GetController;
import it.at.restfs.http.PathResolver;
import it.at.restfs.http.PerRequestContext;
import it.at.restfs.http.PostController;
import it.at.restfs.http.PutController;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.FileSystemContainerRepository;
import it.at.restfs.storage.Storage;
import it.at.restfs.storage.Storage.Implementation;
import it.at.restfs.storage.dto.ResouceNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AkkaModule implements Module {

    @Override
    public void configure(Binder binder) {
        
        final Config config = ConfigFactory.load();

        final ActorSystem actorSystem = ActorSystem.create(PathResolver.APP_NAME, config);
        
        binder
            .bind(ActorSystem.class)
            .toInstance(actorSystem);
        
        binder
            .bind(Config.class)
            .toInstance(config);
        
//        binder
//            .bind(Cluster.class)
//            .toInstance(Cluster.get(actorSystem));
        
        binder
            .bind(MessageDispatcher.class)
            .toInstance(actorSystem.dispatchers().lookup("my-blocking-dispatcher"));
    
        binder
            .bind(ActorMaterializer.class)
            .toInstance(ActorMaterializer.create(actorSystem));
    
        binder
            .bind(Http.class)
            .toInstance(Http.get(actorSystem));
        
        final MapBinder<HttpMethod, Controller> mapBinder = MapBinder.newMapBinder( 
            binder, 
            new TypeLiteral<HttpMethod>() {},
            new TypeLiteral<Controller>() {}
        );
        
        mapBinder.addBinding(HttpMethods.GET).to(GetController.class);
        mapBinder.addBinding(HttpMethods.POST).to(PostController.class);
        mapBinder.addBinding(HttpMethods.PUT).to(PutController.class);
        mapBinder.addBinding(HttpMethods.DELETE).to(DeleteController.class);

        for (Implementation implementation : Storage.Implementation.values()) {
            binder
	            .bind(Key.get(Storage.class, Names.named(implementation.key)))
	            .to(implementation.implClazz);
            	//we can't do this because each implementation wrap it's own connection!!
	            //.in(Singleton.class); 
			
		}
        
        binder.install(
    		new FactoryModuleBuilder()
				.implement(PerRequestContext.class, PerRequestContext.class)				
				.build(PerRequestContext.Factory.class)
		);
        
        binder
            .bind(ContainerRepository.class)
            .to(FileSystemContainerRepository.class)
            .in(Singleton.class);
        
        binder
            .bind(EventRepository.class)
            .to(ShortTimeInMemory.class)
            .in(Singleton.class);

        binder
            .bind(Filter.class)
            .in(Singleton.class);
        
        binder
            .bind(ExceptionHandler.class)
            .toInstance(
                    
                /*

                    da quando uso la direttiva completeOKWithFuture negli endpoint http
                    se ricevo un errore ... tutte le exception sono wrappate da una CompletionException
                    
                    questo mi obbliga a estendere un il normale ExceptionHandler
                    andando a guardare (solo nel caso di CompletionException) la causa vera !!?
                    
                 */
                
                ExceptionHandler.newBuilder()
                    .match(CompletionException.class, x -> { //see https://github.com/akka/akka-http/issues/1267
                        
                        if (x.getCause() instanceof ResouceNotFoundException) {
                            LOGGER.error("handling exception: {}", x.getMessage());
                            
                            return complete(StatusCodes.NOT_FOUND, x.getMessage());
                        }

                        if (x.getCause() instanceof FileAlreadyExistsException) {
                            LOGGER.error("handling exception: {}", x.getMessage());
                            
                            return complete(StatusCodes.CONFLICT, x.getMessage());
                        }
                        
                        return complete(StatusCodes.INTERNAL_SERVER_ERROR);
                        
                    })
                    .match(ResouceNotFoundException.class, x -> {
                        LOGGER.error("handling exception: {}", x.getMessage());
                        
                        return complete(StatusCodes.NOT_FOUND, x.getMessage());
                    })
                    .match(FileAlreadyExistsException.class, x -> {
                        LOGGER.error("handling exception: {}", x.getMessage());
                        
                        return complete(StatusCodes.CONFLICT, x.getMessage());
                    })
                    .build()
                    
            );
                
    }
        
}
