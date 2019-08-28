package it.at.restfs.http;

import com.google.inject.Inject;

import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import it.at.restfs.storage.dto.FileStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DeleteController implements Controller {

//	@Delegate
	private PerRequestContext x;            
	
    //operation = DELETE
    public Route delete(Request t) {
        return x.withFuture(() -> {
            final AssetType typeOf = x.getStorage().typeOf(t.getContainer(), AbsolutePath.of(t.getPath()));        
            
            final FileStatus result = AssetType.FILE == typeOf ? 
                x.getStorage().getStatus(t.getContainer(), t.getPath()) :
                x.getStorage().listStatus(t.getContainer(), t.getPath());
            
            x.getStorage().delete(t.getContainer(), t.getPath());
            
            return result;                    
        });
    }
}
