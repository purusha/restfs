package it.at.restfs.storage;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import it.at.restfs.event.Event;
import it.at.restfs.storage.dto.Container;

public interface ContainerRepository {

    List<UUID> findAll();
    //XXX expose: List<Container> findAll();

    boolean exist(UUID container);
    
    Container load(UUID container);

    void save(Container container);

    void saveCalls(UUID container, List<Event> events);
    
    List<Event> getCalls(UUID container);

    void saveWebhook(UUID container, List<Event> events);

    List<Path> getWebhook(UUID container);
    
    //HttpStatusCode => NumberOf
    void saveStatistics(UUID container, Map<Integer, Long> statistics);
    
    //HttpStatusCode => NumberOf
    Map<Integer, Long> getStatistics(UUID container); 

}
