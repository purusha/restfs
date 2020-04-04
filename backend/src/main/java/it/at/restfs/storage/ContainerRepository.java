package it.at.restfs.storage;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import it.at.restfs.actor.DashboardDataCollectorActor.ContainerData;
import it.at.restfs.event.EventView;
import it.at.restfs.storage.dto.Container;

public interface ContainerRepository {

    List<Container> findAll();

    boolean exist(UUID container);
    
    Container load(UUID container);

    void save(Container container);

    void saveCalls(UUID container, List<EventView> events);
    
    List<EventView> getCalls(UUID container);

    void saveWebhook(UUID container, List<EventView> events);

    //XXX this api should be return a List<EventView>
    List<Path> getWebhook(UUID container);
    
    //HttpStatusCode => NumberOf
    void saveStatistics(UUID container, Map<Integer, Long> statistics);
    
    //HttpStatusCode => NumberOf
    Map<Integer, Long> getStatistics(UUID container); 
    
    void saveDashboardData(List<ContainerData> data);
    
    List<ContainerData> getDashboardData();

}
