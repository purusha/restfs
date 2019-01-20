package it.at.restfs.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.at.restfs.http.HTTPListener.Request;
import lombok.Getter;

@Getter
public class Event {

    private final Request request;
    private final Integer responseCode;
    
    @JsonCreator
    public Event(
        @JsonProperty("request") Request request, 
        @JsonProperty("responseCode") Integer responseCode
    ) {
        this.request = request;
        this.responseCode = responseCode;
    }
    
    @Override
    public String toString() {
        return String.format("response [%s] request [%s]", responseCode, request);
    }
    
}
