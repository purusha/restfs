package it.at.restfs.http;

import static akka.event.Logging.InfoLevel;
import java.util.UUID;
import java.util.function.BiFunction;
import com.google.inject.Inject;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.directives.LogEntry;
import it.at.restfs.actor.EventHandler;
import it.at.restfs.event.Event;
import it.at.restfs.http.HTTPListener.Request;

public class Filter implements BiFunction<HttpRequest, HttpResponse, LogEntry> {
    
    private final ActorSelection eventHandler;

    @Inject
    public Filter(ActorSystem system) {
        this.eventHandler = system.actorSelection("/user/" + EventHandler.ACTOR);
    }

    @Override
    public LogEntry apply(HttpRequest request, HttpResponse response) {
        final String containerId = request.getHeader(HTTPListener.X_CONTAINER).get().value();
        final String path = HTTPListener.getPathString(request.getUri());
        final String operation = request.getUri().query().get(HTTPListener.OP).get();
        
        final Request req = new Request(UUID.fromString(containerId), path, operation);            
        final Event event = new Event(req, response.status());
        
        eventHandler.tell(event, ActorRef.noSender());
        
        return LogEntry.create(
            request.method().name() + ":" + response.status().intValue() +  " " + request.getUri().getPathString(), 
            InfoLevel() //was 3
        );
    }
    
}
