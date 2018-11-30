package it.at.restfs.http;

import java.util.List;
import java.util.function.Function;
import com.google.inject.Inject;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.Storage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public abstract class BaseController implements Function<Request, Route> {
    
    private final Storage storage;
    
    protected abstract List<String> validOperation();
    
    @Override
    public Route apply(Request t) {       
        final String operation = t.getOperation();
        
        if (validOperation().contains(operation)) {
            try {
                return (Route)this.getClass().getDeclaredMethod(operation.toLowerCase(), Request.class).invoke(this, t);
            } catch (Exception e) {
                LOGGER.info("", e);
                throw new RuntimeException("can't handle " + t); //XXX client receive: HTTP/1.1 500 Internal Server Error
            }
        } else {
            throw new RuntimeException("can't handle " + operation); //XXX client receive: HTTP/1.1 500 Internal Server Error
        }
    }    
    
}
