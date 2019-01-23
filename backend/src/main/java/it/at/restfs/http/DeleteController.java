package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.AbsolutePath;
import it.at.restfs.storage.AssetType;
import it.at.restfs.storage.FileStatus;
import it.at.restfs.storage.Storage;

@Singleton
public class DeleteController extends BaseController {

    @Inject
    public DeleteController(Storage storage, MessageDispatcher dispatcher) {
        super(storage, dispatcher);
    }

    //operation = DELETE
    public Route delete(Request t) {
        final AssetType typeOf = getStorage().typeOf(t.getContainer(), AbsolutePath.of(t.getPath()));        
        
        final FileStatus result = AssetType.FILE == typeOf ? 
            getStorage().getStatus(t.getContainer(), t.getPath()) :
            getStorage().listStatus(t.getContainer(), t.getPath());
        
        getStorage().delete(t.getContainer(), t.getPath());
        
        return complete(StatusCodes.OK, result, Jackson.<FileStatus>marshaller());        
    }
}
