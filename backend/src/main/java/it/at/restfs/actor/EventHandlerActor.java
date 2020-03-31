package it.at.restfs.actor;

import java.util.ArrayList;
import java.util.List;
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
import it.at.restfs.http.services.PrometheusCollector;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.dto.Container;
import scala.concurrent.duration.FiniteDuration;

public class EventHandlerActor extends GuiceAbstractActor {
    
    public static final String ACTOR = "EventHandler";
    private final static String CLEAN_UP = "clean-up";
    private final static Function<Event, Integer> EVENT_TO_HTTP_STATUS = (Event event) -> event.getResponseCode();
	
    private final EventRepository eRepo;    
    private final ContainerRepository cRepo;
    private final PrometheusCollector collector;
    
    @Inject
    public EventHandlerActor(EventRepository eRepo, ContainerRepository cRepo, PrometheusCollector collector) {
        this.eRepo = eRepo;
        this.cRepo = cRepo;
        this.collector = collector;
        
        final FiniteDuration apply = FiniteDuration.apply(
            ShortTimeInMemory.expireData() + 1, ShortTimeInMemory.expireUnit()
        );
        
        getContext().system().scheduler().scheduleWithFixedDelay(
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
                              
                final Map<Integer, Long> statistics = cRepo.getStatistics(c.getContainer());
                final List<Event> newEvents = new ArrayList<Event>(c.getEvents());
                
                newEvents.stream()
                    .collect(Collectors.groupingBy(
                        EVENT_TO_HTTP_STATUS
                    ))
                    .entrySet().stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey, entry -> entry.getValue().size()
                        ))
                        .entrySet().stream()
                            .forEach(entry -> {

                            	final Integer key = entry.getKey();
                            	final Integer value = entry.getValue();
                            	
                            	//update local stas file
                                final Long sum = statistics.getOrDefault(key, 0L).longValue() + value.longValue();	                                
                                statistics.put(key, sum); 
                                
                                //update prometheus 
                                collector.metrics(c.getContainer(), key, value);
                            	
                            });
                
                final Container container = cRepo.load(c.getContainer()); 
                    
                if (container.isStatsEnabled()) {                        
                    cRepo.saveStatistics(c.getContainer(), statistics);                                                                               
                }
                
                if (container.isWebHookEnabled()) {
                    cRepo.saveWebhook(c.getContainer(), newEvents);
                }
                
                //append actual events
                newEvents.addAll(cRepo.getCalls(c.getContainer()));                                
                
                //write only first N elements
                cRepo.saveCalls(c.getContainer(), newEvents.subList(0, Math.min(newEvents.size(), 30)));  
                                                
            })
            .matchAny(this::unhandled)
            .build();
    }
}
