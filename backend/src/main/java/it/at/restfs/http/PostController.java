package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.extractRequestEntity;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.AssetType;
import it.at.restfs.storage.Storage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class PostController extends BaseController {
    
    private final ActorMaterializer materializer;

    @Inject
    public PostController(Storage storage, ActorMaterializer materializer) {
        super(storage);
        this.materializer = materializer;
    }
    
    //operation = MKDIRS
    public Route mkdirs(Request t) {        
        getStorage().make(t.getContainer(), t.getPath(), AssetType.FOLDER);
        
        return complete(StatusCodes.OK, Paths.get(t.getPath()), Jackson.<Path>marshaller());            
    }

    //operation = CREATE
    public Route create(Request t) {
        getStorage().make(t.getContainer(), t.getPath(), AssetType.FILE);
        
        return complete(StatusCodes.OK, Paths.get(t.getPath()), Jackson.<Path>marshaller());
    }

    //operation = APPEND
    public Route append(Request t) {
        final Sink<String, CompletionStage<String>> last = Sink.last();        
        
        return extractRequestEntity(request -> {            
            try {
                
                final String body = request.getDataBytes()
                    .map(data -> data.utf8String())
                    .toMat(last, Keep.right())
                    .run(materializer)
                    .toCompletableFuture()
                    .get();
                
                getStorage().append(t.getContainer(), t.getPath(), body);
                
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            
            return complete(StatusCodes.OK, Paths.get(t.getPath()), Jackson.<Path>marshaller());               
        });
    }
    
}
