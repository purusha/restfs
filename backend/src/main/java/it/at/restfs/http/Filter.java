package it.at.restfs.http;

import static akka.event.Logging.InfoLevel;

import java.util.Date;
import java.util.UUID;
import java.util.function.BiFunction;
import com.google.inject.Inject;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.directives.LogEntry;
import it.at.restfs.actor.EventHandlerActor;
import it.at.restfs.event.Event;
import it.at.restfs.http.HTTPListener.Request;
import lombok.extern.slf4j.Slf4j;

import static it.at.restfs.http.PathResolver.getPathString;

@Slf4j
public class Filter implements BiFunction<HttpRequest, HttpResponse, LogEntry> {
	
    private final ActorSelection eventHandler;

    @Inject
    public Filter(ActorSystem system) {
        this.eventHandler = system.actorSelection("/user/" + EventHandlerActor.ACTOR);
    }

    @Override
    public LogEntry apply(HttpRequest request, HttpResponse response) {
    	LOGGER.info("{}", new Date());
    	
        final String containerId = request.getHeader(HTTPListener.X_CONTAINER).get().value();
        final String path = getPathString(request.getUri());

        //XXX if /stats endpoint is called ... OP param is not resolved
        final String operation = request.getUri().query().get(HTTPListener.OP).orElse(null);
        final Request req = new Request(UUID.fromString(containerId), path, operation);            
        final Event event = new Event(req, response.status().intValue());
        
        eventHandler.tell(event, ActorRef.noSender());            
        
        return LogEntry.create(
            request.method().name() + ":" + response.status().intValue() +  " " + request.getUri().getPathString(), 
            InfoLevel() //was 3
        );
    }
    
}
