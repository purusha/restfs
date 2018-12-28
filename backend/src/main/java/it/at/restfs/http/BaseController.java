package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import com.google.inject.Inject;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.FileStatus;
import it.at.restfs.storage.FolderStatus;
import it.at.restfs.storage.Storage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


@Getter
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public abstract class BaseController implements Function<Request, Route> {
        
    private final Storage storage;
    
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
    
    protected Route getFileStatus(Request t) {
        final FileStatus result = getStorage().getStatus(t.getContainer(), t.getPath());
        
        return complete(StatusCodes.OK, result, Jackson.<FileStatus>marshaller());
    }
    
    protected Route getDirectoryStatus(Request t) {
        final FolderStatus result = getStorage().listStatus(t.getContainer(), t.getPath());
        
        return complete(StatusCodes.OK, result, Jackson.<FolderStatus>marshaller());
    }
    
}
