package it.at.restfs.event;

import it.at.restfs.http.services.PathHelper.Request;
import lombok.Getter;

@Getter
public class Event {

    private final Request request;
    private final Integer responseCode;
    private final Long occurred;
    
    public Event(
        Request request, 
        Integer responseCode
    ) {
        this.request = request;
        this.responseCode = responseCode;
        this.occurred = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return String.format("response [%s] request [%s]", responseCode, request);
    }
    
}
