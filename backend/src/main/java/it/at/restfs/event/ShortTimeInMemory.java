package it.at.restfs.event;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import it.at.restfs.actor.EventHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShortTimeInMemory implements EventRepository {
    
    /*

        mantiene un buffer delle ultime N operazioni su un container,
        raggruppandole per questo ... in modo tale che quando l'attore
        che scrive le riceve scrive una sola volta il file yaml !!
 
     */
    
    private final Cache<UUID, List<Event>> cache;
    
    private final ActorSelection eventHandler;

    @Inject
    public ShortTimeInMemory(ActorSystem system) {
        
        eventHandler = system.actorSelection("/user/" + EventHandler.ACTOR);

        cache = Caffeine.newBuilder()
            .maximumSize(1_000) //number of entries
            .expireAfterWrite(30, TimeUnit.SECONDS) //short in memory was here
            .writer(new CacheWriter<UUID, List<Event>>() {

                @Override
                public void write(UUID key, List<Event> value) {
                }

                @Override
                public void delete(UUID key, List<Event> value, RemovalCause cause) {
                    LOGGER.debug("delete {} => {} with {}", key, value, cause);
                                        
                    eventHandler.tell(new ContainerEvents(key, value), ActorRef.noSender());
                }
                
            })
            .build();
        
    }

    @Override
    public void save(Event e) {
        cache
            .get(e.getRequest().getContainer(), uuid -> Lists.newArrayList())
            .add(e);
    }

    //XXX call this every X seconds !!? from Actor
    @Override
    public void cleanUp() {
        cache.cleanUp();
    }

}
