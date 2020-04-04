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
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.ExceptionHandler;
import akka.stream.Materializer;
import it.at.restfs.auth.AuthorizationChecker;
import it.at.restfs.auth.AuthorizationMaker;
import it.at.restfs.auth.AuthorizationManager;
import it.at.restfs.auth.Authorized;
import it.at.restfs.auth.AuthorizedInterceptor;
import it.at.restfs.event.EventRepository;
import it.at.restfs.event.ShortTimeInMemory;
import it.at.restfs.http.api.ControllerRunner;
import it.at.restfs.http.services.Complete;
import it.at.restfs.http.services.Filter;
import it.at.restfs.http.services.PathHelper;
import it.at.restfs.http.services.PerRequestContext;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.FileSystemContainerRepository;
import it.at.restfs.storage.RootFileSystem;
import it.at.restfs.storage.Storage;
import it.at.restfs.storage.Storage.Implementation;
import it.at.restfs.storage.StorageFactory;
import it.at.restfs.storage.dto.ResouceNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AkkaModule implements Module {

    @Override
    public void configure(Binder binder) {
        
        final Config config = ConfigFactory.load();

        final ActorSystem actorSystem = ActorSystem.create(PathHelper.APP_NAME, config);
        
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
            .bind(Materializer.class)
            .toInstance(Materializer.createMaterializer(actorSystem));
    
        binder
            .bind(Http.class)
            .toInstance(Http.get(actorSystem));
                        
        final MapBinder<Implementation, StorageFactory<?>> storages = MapBinder.newMapBinder(
    		binder, new TypeLiteral<Implementation>(){}, new TypeLiteral<StorageFactory<?>>(){}
		);
   
        for (Storage.Implementation i : Storage.Implementation.values()) {       
        	
        	binder.install(
    			new FactoryModuleBuilder().build(i.factory)
			);
        	
        	storages.addBinding(i).to(i.factory);			
		}
        
        for (AuthorizationChecker.Implementation i : AuthorizationChecker.Implementation.values()) {
        	
            binder
	            .bind(Key.get(AuthorizationChecker.class, Names.named(i.name())))
	            .to(i.c)
	            .in(Singleton.class); 

            binder
	            .bind(Key.get(AuthorizationMaker.class, Names.named(i.name())))
	            .to(i.m)
	            .in(Singleton.class); 
            
		}        
        
        binder.install(
    		new FactoryModuleBuilder().build(PerRequestContext.Factory.class)
		);
        
        binder
        	.bind(RootFileSystem.class)
        	.in(Singleton.class);        	
        
        binder
        	.bind(AuthorizationManager.class)
        	.in(Singleton.class);
        
        binder
        	.bind(ControllerRunner.class)
        	.in(Singleton.class);
        
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
        
        final AuthorizedInterceptor i = new AuthorizedInterceptor();
        binder.requestInjection(i);
        
        binder
        	.bindInterceptor(Matchers.any(), Matchers.annotatedWith(Authorized.class), i);
        
        binder
            .bind(ExceptionHandler.class)
            .toInstance(
                    
                /*

                    da quando uso la direttiva completeOKWithFuture negli endpoint http
                    se ricevo un errore ... tutte le exception sono wrappate da una CompletionException
                    
                    questo mi obbliga a estendere il normale ExceptionHandler
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
                        
                        return Complete.internalError();
                        
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
