package it.at.restfs.actor;

import com.google.inject.Inject;
import it.at.restfs.event.ContainerEvents;
import it.at.restfs.event.Event;
import it.at.restfs.event.EventRepository;
import it.at.restfs.guice.GuiceAbstractActor;
import it.at.restfs.storage.Container;
import it.at.restfs.storage.ContainerRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EventHandler extends GuiceAbstractActor {
    public static final String ACTOR = "EventHandler";
    
    private final EventRepository eRepo;
    
    private final ContainerRepository cRepo;
    
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(Event.class, e -> {
                
                eRepo.save(e);
                
            })
            .match(ContainerEvents.class, c -> {
                
                /*
    
                    use this information for:
                    
                    1) write statistical info for each container (use yaml file ???)
                    2) write webhook info to be sent remotely 
                    3) write last N call available 
                    
               */    

                final Container container = cRepo.load(c.getContainer());
                
            })
            .matchAny(this::unhandled)
            .build();
    }
}
