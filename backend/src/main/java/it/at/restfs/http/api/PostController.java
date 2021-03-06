package it.at.restfs.http.api;

import static akka.http.javadsl.server.Directives.decodeRequest;
import static akka.http.javadsl.server.Directives.extractRequestEntity;

import com.google.inject.Inject;

import akka.http.javadsl.server.Route;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import it.at.restfs.http.services.PathHelper.Request;
import it.at.restfs.http.services.PerRequestContext;
import it.at.restfs.storage.dto.AssetType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PostController implements Controller {
    
	private PerRequestContext x;            
    
    //operation = MKDIRS
    public Route mkdirs(Request t) {
        return x.withFuture(() -> {        
        	x.getStorage().make(t.getPath(), AssetType.FOLDER);
            
            return x.getStorage().listStatus(t.getPath());
        });
    }

    //operation = CREATE
    public Route create(Request t) {
        return x.withFuture(() -> {        
        	x.getStorage().make(t.getPath(), AssetType.FILE);
            
            return x.getStorage().getStatus(t.getPath());        
        });
    }

    //operation = APPEND
    public Route append(Request t) {
        return decodeRequest(() -> //gzipped body content is decoded by this ... when text do nothing !
            extractRequestEntity(request -> {
                return x.withFuture(() -> {
                	x.getStorage().append(t.getPath(), getBody(request.getDataBytes()));
                    
                    return x.getStorage().getStatus(t.getPath());                                        
                });
            })
        );            
    }
    
    @SneakyThrows
    private String getBody(Source<ByteString, Object> source) {
        return source.map(data -> data.utf8String())
            .toMat(Sink.last(), Keep.right())
            .run(x.getMaterializer())
            .toCompletableFuture()
            .get();                                                        
    }
    
}
