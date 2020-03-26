package it.at.restfs.http.services;

import static akka.http.javadsl.server.Directives.completeOKWithFuture;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.Route;
import akka.stream.Materializer;
import it.at.restfs.http.services.PathHelper.Request;
import it.at.restfs.storage.Storage;
import it.at.restfs.storage.StorageResolver;
import lombok.AccessLevel;
import lombok.Getter;

@Getter(value = AccessLevel.PUBLIC)
public class PerRequestContext {
	
	public interface Factory {
		PerRequestContext create(Request request);
	}
    
    private final Storage storage;
    private final Materializer materializer;
    private final MessageDispatcher dispatcher;    
    
    @Inject
    private PerRequestContext(Materializer materializer, StorageResolver resolver, MessageDispatcher dispatcher, @Assisted Request request) {    	
		this.storage = resolver.get(request.getContainer()); //XXX this is the "per request" scope handmade
    	this.dispatcher = dispatcher;
    	this.materializer = materializer;
	}
    
    //see https://doc.akka.io/docs/akka-http/current/handling-blocking-operations-in-akka-http-routes.html
    public <T> Route withFuture(Supplier<T> supplier) {
        return completeOKWithFuture(
            CompletableFuture.supplyAsync(supplier, dispatcher),
            Jackson.<T>marshaller()
        );        
    }
        
}
