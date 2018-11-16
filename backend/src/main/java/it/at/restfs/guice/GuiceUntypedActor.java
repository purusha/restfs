package it.at.restfs.guice;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;

import com.google.inject.Injector;

public abstract class GuiceUntypedActor extends UntypedAbstractActor {

    private Injector getInjector() {
        return GuiceExtension.provider.get(getContext().system()).getInjector();
    }

    public Props makeGuiceProps(Class<?> clazz) {
        return Props.create(GuiceActorProducer.class, getInjector(), clazz);
    }
}
