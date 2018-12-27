package it.at.restfs.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.AbsolutePath;
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
        final AssetType typeOf = getStorage().typeOf(t.getContainer(), AbsolutePath.of(t.getPath()));        
        
        final Route status = AssetType.FILE == typeOf ?
            getFileStatus(t) :
            getDirectoryStatus(t);

        getStorage().delete(t.getContainer(), t.getPath());
        
        return status;
    }
}
