package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.Storage;
import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;

@Singleton
public class GetController extends BaseController {
            
    @Inject
    public GetController(Storage storage, MessageDispatcher dispatcher) {
        super(storage, dispatcher);
    }

    //operation = OPEN (this is download)
    public Route open(Request t) {                
        if(AssetType.FOLDER == getStorage().typeOf(t.getContainer(), AbsolutePath.of(t.getPath()))) {
            return complete(StatusCodes.BAD_REQUEST, "dowload is available only for file objects");
        }
        
        return withFuture(() -> {
            
            /*
             * XXX this implementation is too stupid ... how to meet the correct file encoding !!?
             */
            
            return getStorage().open(t.getContainer(), t.getPath());
        });
    }

    //operation = GETSTATUS
    protected Route getstatus(Request t) {
        return withFuture(() -> {
            return getStorage().getStatus(t.getContainer(), t.getPath());
        });        
    }
    
    //operation = LISTSTATUS
    protected Route liststatus(Request t) {        
        return withFuture(() -> {
            return getStorage().listStatus(t.getContainer(), t.getPath());
        });        
    }

}
