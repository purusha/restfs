package it.at.restfs.event;

public interface EventRepository {

    void save(Event e);
    
    void cleanUp();

}
