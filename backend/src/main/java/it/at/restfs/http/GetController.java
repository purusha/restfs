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
import it.at.restfs.storage.OpenFile;
import it.at.restfs.storage.Storage;

@Singleton
public class GetController extends BaseController {
            
    @Inject
    public GetController(Storage storage, MessageDispatcher dispatcher) {
        super(storage, dispatcher);
    }

    //operation = OPEN (this is download)
    public Route open(Request t) {
        
        /*
         * XXX this implementation is too stupid ... how to meet the correct file encoding !!?
         */
        
        if(AssetType.FOLDER == getStorage().typeOf(t.getContainer(), AbsolutePath.of(t.getPath()))) {
            return complete(StatusCodes.BAD_REQUEST, "dowload is available only for file objects");
        }
        
        final OpenFile result = getStorage().open(t.getContainer(), t.getPath());
        
        return complete(StatusCodes.OK, result, Jackson.<OpenFile>marshaller());
    }

    //operation = LISTSTATUS
    public Route liststatus(Request t) {        
        return getDirectoryStatus(t);
    }

    //operation = GETSTATUS
    public Route getstatus(Request t) {
        return getFileStatus(t);
    }

}
