package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;

import com.google.inject.Inject;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.services.PathHelper.Request;
import it.at.restfs.http.services.PerRequestContext;
import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GetController implements Controller {
	
	private PerRequestContext x;            

    //operation = OPEN (this is download)
    public Route open(Request t) {                
        if(AssetType.FOLDER == x.getStorage().typeOf(t.getContainer(), AbsolutePath.of(t.getPath()))) {
            return complete(StatusCodes.BAD_REQUEST, "dowload is available only for file objects");
        }
        
        return x.withFuture(() -> {
            
            /*
             * XXX this implementation is too stupid ... how to meet the correct file encoding !!?
             */
            
            return x.getStorage().open(t.getContainer(), t.getPath());
        });
    }

    //operation = GETSTATUS
    public Route getstatus(Request t) {
        return x.withFuture(() -> {
            return x.getStorage().getStatus(t.getContainer(), t.getPath());
        });        
    }
    
    //operation = LISTSTATUS
    public Route liststatus(Request t) {        
        return x.withFuture(() -> {
            return x.getStorage().listStatus(t.getContainer(), t.getPath());
        });        
    }

}
