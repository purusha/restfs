package it.at.restfs.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.Storage;
import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import it.at.restfs.storage.dto.FileStatus;

@Singleton
public class DeleteController extends BaseController {

    @Inject
    public DeleteController(Storage storage, MessageDispatcher dispatcher) {
        super(storage, dispatcher);
    }

    //operation = DELETE
    public Route delete(Request t) {
        return withFuture(() -> {
            final AssetType typeOf = getStorage().typeOf(t.getContainer(), AbsolutePath.of(t.getPath()));        
            
            final FileStatus result = AssetType.FILE == typeOf ? 
                getStorage().getStatus(t.getContainer(), t.getPath()) :
                getStorage().listStatus(t.getContainer(), t.getPath());
            
            getStorage().delete(t.getContainer(), t.getPath());
            
            return result;                    
        });
    }
}
