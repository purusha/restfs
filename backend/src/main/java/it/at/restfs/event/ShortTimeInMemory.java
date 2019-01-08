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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShortTimeInMemory implements EventRepository {
    
    private final Cache<UUID, List<Event>> cache;

    @Inject
    public ShortTimeInMemory() {

        cache = Caffeine.newBuilder()
            .maximumSize(100) //by container ?
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .writer(new CacheWriter<UUID, List<Event>>() {

                @Override
                public void write(UUID key, List<Event> value) {
                    LOGGER.debug("write {} => {}", key, value);
                }

                @Override
                public void delete(UUID key, List<Event> value, RemovalCause cause) {
                    LOGGER.debug("delete {} => {} with {}", key, value, cause);
                    
                    /*

                         use this information for:
                         
                         1) write statistical info for each container (use yaml file ???)
                         2) write webhook info to be sent remotely 
                         
                     */
                    
                }
                
            })
            .build();
        
        cache.cleanUp();
        
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
