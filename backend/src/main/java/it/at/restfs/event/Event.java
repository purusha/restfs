package it.at.restfs.event;

import com.google.inject.Inject;
import akka.http.javadsl.model.StatusCode;
import it.at.restfs.http.HTTPListener.Request;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Event {

    private final Request request;
    private final StatusCode responseCode;
    
    @Override
    public String toString() {
        return String.format("response [%s] request [%s]", responseCode, request);
    }
    
}
