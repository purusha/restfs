package it.at.restfs.actor;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.google.inject.Inject;
import akka.actor.ActorRef;
import it.at.restfs.event.ContainerEvents;
import it.at.restfs.event.Event;
import it.at.restfs.event.EventRepository;
import it.at.restfs.event.ShortTimeInMemory;
import it.at.restfs.guice.GuiceAbstractActor;
import it.at.restfs.storage.Container;
import it.at.restfs.storage.ContainerRepository;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

@Slf4j
public class EventHandlerActor extends GuiceAbstractActor {
    
    public static final String ACTOR = "EventHandler";
    private final static String CLEAN_UP = "clean-up";
    private final static Function<Event, Integer> EVENT_TO_HTTP_STATUS = (Event event) -> event.getResponseCode();

    private final EventRepository eRepo;    
    private final ContainerRepository cRepo;

    @Inject
    public EventHandlerActor(EventRepository eRepo, ContainerRepository cRepo) {
        this.eRepo = eRepo;
        this.cRepo = cRepo;
        
        final FiniteDuration apply = FiniteDuration.apply(
            ShortTimeInMemory.expireData() + 1, ShortTimeInMemory.expireUnit()
        );
        
        getContext().system().scheduler().schedule(
            apply, apply, getSelf(), CLEAN_UP, getContext().system().dispatcher(), ActorRef.noSender()
        );
    }
        
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .matchEquals(CLEAN_UP, m -> {
                eRepo.cleanUp();
            })
            .match(Event.class, e -> {
                eRepo.save(e);
            })
            .match(ContainerEvents.class, c -> {
                
                /*
    
                    use this information to:
                    
                    1) write statistical info for each container (use yaml file ???)
                    2) write webhook info to be sent remotely 
                    3) write last N call available 
                    
                */    

                final Container container = cRepo.load(c.getContainer());                
                LOGGER.info("load container {} for {}", container, c);
                
                if (container.isStatsEnabled()) {
                    final Map<Integer, Long> statistics = container.getStatistics();

                    c.getEvents().stream()
                        .collect(Collectors.groupingBy(
                            EVENT_TO_HTTP_STATUS
                        ))
                        .entrySet().stream()
                            .collect(Collectors.toMap(
                                Map.Entry::getKey, entry -> entry.getValue().size()
                            ))
                            .entrySet().stream()
                            .forEach(entry -> {                    
                                final Long sum = statistics.getOrDefault(entry.getKey(), 0L).longValue() + entry.getValue().longValue();
                                
                                statistics.put(entry.getKey(), sum);
                            });
                    
                    cRepo.save(container);
                }
                
                if (container.isWebHookEnabled()) {
                    cRepo.saveWebhook(c.getContainer(), c.getEvents());
                }
                
                cRepo.saveCalls(c.getContainer(), c.getEvents());
                                                
            })
            .matchAny(this::unhandled)
            .build();
    }
}
