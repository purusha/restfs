package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import java.util.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.AssetType;
import it.at.restfs.storage.Storage;

@Singleton
public class DeleteController extends BaseController {

    @Inject
    public DeleteController(Storage storage) {
        super(storage);
    }

    //operation = DELETE
    public Route delete(Request t) {        
        final Optional<AssetType> typeOf = getStorage().typeOf(t.getContainer(), t.getPath());        
        if (!typeOf.isPresent()) {
            return complete(StatusCodes.NOT_FOUND);    
        }
        
        final Route status = AssetType.FILE == typeOf.get() ?
            getFileStatus(t) :
            getDirectoryStatus(t);

        getStorage().delete(t.getContainer(), t.getPath());
        
        return status;
    }
}
