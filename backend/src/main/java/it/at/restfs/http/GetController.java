package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.Storage;

@Singleton
public class GetController extends BaseController {
    
//    enum Operation {
//        GETSTATUS, LISTSTATUS, OPEN,                        //GET
//        MKDIRS, CREATE, SETPERMISSION, SETOWNER, RENAME,    //PUT
//        APPEND,                                             //POST
//        DELETE                                              //DELETE
//    }    
        
    @Inject
    public GetController(Storage storage) {
        super(storage);
    }

    protected Route open(Request t) {
        getStorage().open(t.getContainer(), t.getPath());
        
        return complete(
            String.format("Container %s\nUri %s\nOperation %s\n", t.getContainer(), t.getPath(), t.getOperation())
        );        
    }

    protected Route liststatus(Request t) {        
        getStorage().listStatus(t.getContainer(), t.getPath());
        
        return complete(
            String.format("Container %s\nUri %s\nOperation %s\n", t.getContainer(), t.getPath(), t.getOperation())
        );        
    }

    protected Route getstatus(Request t) {
        getStorage().getStatus(t.getContainer(), t.getPath());
        
        return complete(
            String.format("Container %s\nUri %s\nOperation %s\n", t.getContainer(), t.getPath(), t.getOperation())
        );        
    }

    @Override
    protected List<String> validOperation() {
        return Lists.newArrayList("GETSTATUS", "LISTSTATUS", "OPEN");
    }
    
}
