package it.at.restfs.actor;

import com.google.inject.Inject;
import it.at.restfs.event.Event;
import it.at.restfs.event.EventRepository;
import it.at.restfs.guice.GuiceAbstractActor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EventHandler extends GuiceAbstractActor {
    public static final String ACTOR = "EventHandler";
    
    private final EventRepository repository;
    
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(Event.class, e -> {
                
                repository.save(e);
                
            })
            .matchAny(this::unhandled)
            .build();
    }
}
