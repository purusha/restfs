package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.decodeRequest;
import static akka.http.javadsl.server.Directives.extractRequestEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.AssetType;
import it.at.restfs.storage.Storage;
import lombok.SneakyThrows;

@Singleton
public class PostController extends BaseController {
    
    private final ActorMaterializer materializer;

    @Inject
    public PostController(Storage storage, ActorMaterializer materializer, MessageDispatcher dispatcher) {
        super(storage, dispatcher);
        
        this.materializer = materializer;
    }
    
    //operation = MKDIRS
    public Route mkdirs(Request t) {
        return withFuture(() -> {        
            getStorage().make(t.getContainer(), t.getPath(), AssetType.FOLDER);
            
            return getStorage().listStatus(t.getContainer(), t.getPath());
        });
    }

    //operation = CREATE
    public Route create(Request t) {
        return withFuture(() -> {        
            getStorage().make(t.getContainer(), t.getPath(), AssetType.FILE);
            
            return getStorage().getStatus(t.getContainer(), t.getPath());        
        });
    }

    //operation = APPEND
    public Route append(Request t) {
        return decodeRequest(() -> //gzipped body content is decoded by this ... when text do nothing !
            extractRequestEntity(request -> {
                return withFuture(() -> {
                    getStorage().append(t.getContainer(), t.getPath(), getBody(request.getDataBytes()));
                    
                    return getStorage().getStatus(t.getContainer(), t.getPath());                                        
                });
            })
        );            
    }
    
    @SneakyThrows
    private String getBody(Source<ByteString, Object> source) {
        return source.map(data -> data.utf8String())
            .toMat(Sink.last(), Keep.right())
            .run(materializer)
            .toCompletableFuture()
            .get();                                                        
    }
    
}
