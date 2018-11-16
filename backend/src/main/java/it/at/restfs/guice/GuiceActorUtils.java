package it.at.restfs.guice;

import akka.actor.ActorSystem;
import akka.actor.Props;

import com.google.inject.Injector;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GuiceActorUtils {

    private static Injector getInjector(ActorSystem actorSystem) {
        return GuiceExtension.provider.get(actorSystem).getInjector();
    }

    public static Props makeProps(ActorSystem actorSystem, Class<?> clazz) {
        return Props.create(GuiceActorProducer.class, getInjector(actorSystem), clazz);
    }
}