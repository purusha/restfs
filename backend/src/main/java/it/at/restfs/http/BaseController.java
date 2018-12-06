package it.at.restfs.http;

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
    
    /*

    enum Operation {
        GETSTATUS, LISTSTATUS, OPEN,                        //GET
        SETPERMISSION, SETOWNER, RENAME,                    //PUT
        MKDIRS, CREATE, APPEND,                             //POST
        DELETE,                                             //DELETE
    }
    
    */        
    
    private final Storage storage;
    
    @Override
    public Route apply(Request t) {       
        final String operation = t.getOperation();
        
        try {
            return (Route)this.getClass().getDeclaredMethod(operation.toLowerCase(), Request.class).invoke(this, t);
        } catch (Exception e) {
            LOGGER.info("", e.getCause()); //java.lang.reflect.InvocationTargetException: null ... https://stackoverflow.com/questions/6020719/what-could-cause-java-lang-reflect-invocationtargetexception     
            throw new RuntimeException("can't handle " + t); //XXX client receive: HTTP/1.1 500 Internal Server Error
        }
    }    
    
}
