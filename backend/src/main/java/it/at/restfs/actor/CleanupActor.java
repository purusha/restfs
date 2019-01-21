package it.at.restfs.actor;

import java.nio.file.Files;
import java.nio.file.Path;
import it.at.restfs.guice.GuiceAbstractActor;

public class CleanupActor extends GuiceAbstractActor {
    
    public static final String ACTOR = "Cleanup";

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(Path.class, p -> {
                Files.delete(p);
            })
            .matchAny(this::unhandled)
            .build();
    }
    
}
