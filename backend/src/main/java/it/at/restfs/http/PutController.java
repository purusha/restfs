package it.at.restfs.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.Storage;

@Singleton
public class PutController extends BaseController {

    @Inject
    public PutController(Storage storage) {
        super(storage);
    }

    //operation = RENAME
    public Route rename(Request t) {
        return null;
    }
    

    //operation = move
    public Route move(Request t) {
        
        /*
             support to move file/directory in another existing directory
         */
        
        return null;
    }
    
}
