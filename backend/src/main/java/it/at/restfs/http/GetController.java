package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.Storage;

@Singleton
public class GetController extends BaseController {
            
    @Inject
    public GetController(Storage storage) {
        super(storage);
    }

    //operation = OPEN
    public Route open(Request t) {
        getStorage().open(t.getContainer(), t.getPath());
        
        return complete(
            String.format("Container %s\nUri %s\nOperation %s\n", t.getContainer(), t.getPath(), t.getOperation())
        );        
    }

    //operation = LISTSTATUS
    public Route liststatus(Request t) {        
        getStorage().listStatus(t.getContainer(), t.getPath());
        
        return complete(
            String.format("Container %s\nUri %s\nOperation %s\n", t.getContainer(), t.getPath(), t.getOperation())
        );        
    }

    //operation = GETSTATUS
    public Route getstatus(Request t) {
        getStorage().getStatus(t.getContainer(), t.getPath());
        
        return complete(
            String.format("Container %s\nUri %s\nOperation %s\n", t.getContainer(), t.getPath(), t.getOperation())
        );        
    }

}
