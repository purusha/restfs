package it.at.restfs.http.services;

import static akka.event.Logging.InfoLevel;
import static it.at.restfs.http.services.PathHelper.build;
import static it.at.restfs.http.services.PathHelper.getPathString;

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
import it.at.restfs.http.api.HTTPListener;
import it.at.restfs.storage.dto.AbsolutePath;

public class Filter implements BiFunction<HttpRequest, HttpResponse, LogEntry> {
	
    private final ActorSelection eventHandler;

    @Inject
    public Filter(ActorSystem system) {
        this.eventHandler = system.actorSelection("/user/" + EventHandlerActor.ACTOR);
    }

    @Override
    public LogEntry apply(HttpRequest request, HttpResponse response) {
        final String containerId = request.getHeader(HTTPListener.X_CONTAINER).get().value();
        final AbsolutePath path = getPathString(request.getUri());
        final String operation = request.getUri().query().get(HTTPListener.OP).orElse(null);   
                
        eventHandler.tell(        
    		new Event(
        		build(UUID.fromString(containerId), path, operation), 
        		response.status().intValue()
    		), 
    		ActorRef.noSender()
		);     
        
        return LogEntry.create(
            request.method().name() + ":" + response.status().intValue() +  " " + request.getUri().getPathString(), 
            InfoLevel() //was 3
        );
    }
    
}
