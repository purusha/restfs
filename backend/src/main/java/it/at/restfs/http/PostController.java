package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.decodeRequest;
import static akka.http.javadsl.server.Directives.extractRequestEntity;
import java.util.concurrent.ExecutionException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
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
        
        return getDirectoryStatus(t);
    }

    //operation = CREATE
    public Route create(Request t) {
        getStorage().make(t.getContainer(), t.getPath(), AssetType.FILE);
        
        return getFileStatus(t);
    }

    //operation = APPEND
    public Route append(Request t) {
        return decodeRequest(() -> //gzipped body content is decoded by this ... when text do nothing !
            extractRequestEntity(request -> {                    
                try {
                    
                    getStorage().append(
                        t.getContainer(), t.getPath(), getBody(request.getDataBytes())
                    );
                                                    
                    return getFileStatus(t);
                    
                } catch (ExecutionException | InterruptedException e) {
                    LOGGER.error("", e);
                    
                    return complete(StatusCodes.INTERNAL_SERVER_ERROR);
                }                                                           
            })
        );            
    }
    
    //blocking 
    private String getBody(Source<ByteString, Object> source) throws InterruptedException, ExecutionException {
        return source.map(data -> data.utf8String())
            .toMat(Sink.last(), Keep.right())
            .run(materializer)
            .toCompletableFuture()
            .get();                                                        
    }
    
}
