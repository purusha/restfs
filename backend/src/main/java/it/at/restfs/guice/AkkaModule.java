package it.at.restfs.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.typesafe.config.ConfigFactory;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.stream.ActorMaterializer;

public class AkkaModule implements Module {

    @Override
    public void configure(Binder binder) {

        final ActorSystem actorSystem = ActorSystem.create("engine", ConfigFactory.load());
        
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
        
        
    }

}
