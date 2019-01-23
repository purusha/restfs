package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.completeOKWithFuture;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import com.google.inject.Inject;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.Storage;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public abstract class BaseController implements Function<Request, Route> {
        
    private final Storage storage;
    private final MessageDispatcher dispatcher;
    
    @Inject
    public BaseController(Storage storage, MessageDispatcher dispatcher) {
        this.storage = storage;
        this.dispatcher = dispatcher;
    }    
    
    @SneakyThrows(Throwable.class)
    @Override
    public Route apply(Request t) {       
        final String operation = t.getOperation();
        
        try {
            return (Route) this.getClass().getDeclaredMethod(operation.toLowerCase(), Request.class).invoke(this, t);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
            throw e;
        } catch (InvocationTargetException e) { //XXX important ... because we use Reflection !!!?
            throw e.getCause();
        }
    }    
        
    //see https://doc.akka.io/docs/akka-http/current/handling-blocking-operations-in-akka-http-routes.html
    protected <T> Route withFuture(Supplier<T> supplier) {
        return completeOKWithFuture(
            CompletableFuture.supplyAsync(supplier, dispatcher),
            Jackson.<T>marshaller()
        );        
    }
        
}
