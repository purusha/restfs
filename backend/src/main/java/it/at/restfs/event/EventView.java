package it.at.restfs.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.at.restfs.http.services.PathHelper.RequestView;
import lombok.Getter;

@Getter
public class EventView {
	
    private final RequestView request;
    private final Integer responseCode;
    private final Long occurred;
	
    @JsonCreator
    public EventView(
        @JsonProperty("request") RequestView request, 
        @JsonProperty("responseCode") Integer responseCode,
        @JsonProperty("occurred") Long occurred
    ) {
        this.request = request;
        this.responseCode = responseCode;
        this.occurred = occurred;
    }    

}
