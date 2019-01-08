package it.at.restfs.guice;

import static akka.http.javadsl.server.Directives.complete;
import java.nio.file.FileAlreadyExistsException;
import java.util.function.Function;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.typesafe.config.ConfigFactory;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import it.at.restfs.event.EventRepository;
import it.at.restfs.event.ShortTimeInMemory;
import it.at.restfs.http.DeleteController;
import it.at.restfs.http.GetController;
import it.at.restfs.http.HTTPListener;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.http.PostController;
import it.at.restfs.http.PutController;
import it.at.restfs.storage.FileSystemStorage;
import it.at.restfs.storage.ResouceNotFoundException;
import it.at.restfs.storage.Storage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AkkaModule implements Module {

    @Override
    public void configure(Binder binder) {

        final ActorSystem actorSystem = ActorSystem.create(HTTPListener.APP_NAME, ConfigFactory.load());
        
        binder
            .bind(ActorSystem.class)
            .toInstance(actorSystem);
        
//        binder
//            .bind(Cluster.class)
//            .toInstance(Cluster.get(actorSystem));
    
        binder
            .bind(ActorMaterializer.class)
            .toInstance(ActorMaterializer.create(actorSystem));
    
        binder
            .bind(Http.class)
            .toInstance(Http.get(actorSystem));
        
        final MapBinder<HttpMethod, Function<Request, Route>> mapBinder = MapBinder.newMapBinder(
            binder, 
            new TypeLiteral<HttpMethod>() {},
            new TypeLiteral<Function<Request, Route>>() {}
        );
        
        mapBinder.addBinding(HttpMethods.GET).to(GetController.class);
        mapBinder.addBinding(HttpMethods.POST).to(PostController.class);
        mapBinder.addBinding(HttpMethods.PUT).to(PutController.class);
        mapBinder.addBinding(HttpMethods.DELETE).to(DeleteController.class);
        
        binder
            .bind(Storage.class)
            .to(FileSystemStorage.class)
            .in(Singleton.class);
            
        binder
            .bind(EventRepository.class)
            .to(ShortTimeInMemory.class)
            .in(Singleton.class);
        
        binder
            .bind(ExceptionHandler.class)
            .toInstance(
                
                ExceptionHandler.newBuilder()
                    .match(ResouceNotFoundException.class, x -> {
                        LOGGER.error("handling exception: ", x);
                        
                        return complete(StatusCodes.NOT_FOUND, x.getMessage());
                    })
                    .match(FileAlreadyExistsException.class, x -> {
                        LOGGER.error("handling exception: ", x);
                        
                        return complete(StatusCodes.CONFLICT, x.getMessage());
                    })
                    .build()
                    
            );
                
    }

}
