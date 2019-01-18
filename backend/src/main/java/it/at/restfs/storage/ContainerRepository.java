package it.at.restfs.storage;

import java.util.List;
import java.util.UUID;
import it.at.restfs.event.Event;

public interface ContainerRepository {

    Container load(UUID container);

    void save(Container container);

    void saveWebhook(UUID container, List<Event> events);

    void saveCalls(UUID container, List<Event> events);

}
