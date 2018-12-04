package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.PathMatchers.*;
import java.util.function.Function;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.Storage;

@Singleton
public class PostController extends BaseController {

    @Inject
    public PostController(Storage storage) {
        super(storage);
    }
    
    /*
        String content = "Hello World !!";
         
        Files.write(Paths.get("c:/output.txt"), content.getBytes()); 
     */

}
