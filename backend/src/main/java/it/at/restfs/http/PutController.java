package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.parameter;
import org.apache.commons.lang3.StringUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.Storage;
import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;

@Singleton
public class PutController extends BaseController {

    @Inject
    public PutController(Storage storage, MessageDispatcher dispatcher) {
        super(storage, dispatcher);
    }

    //operation = RENAME
    public Route rename(Request t) {        
        /*
             rename file o folder
         */
        
        return parameter("target", target -> {
            if(StringUtils.indexOfAny(target, "\\/") >= 0) { //XXX target non può essere un path
                return complete(StatusCodes.BAD_REQUEST, "target cannot be a directory");
            }
            
            return withFuture(() -> {
                final String result = getStorage().rename(t.getContainer(), t.getPath(), target);
                final AssetType typeOf = getStorage().typeOf(t.getContainer(), AbsolutePath.of(result));
                
                return AssetType.FILE == typeOf ? 
                    getStorage().getStatus(t.getContainer(), result) :
                    getStorage().listStatus(t.getContainer(), result);                    
            });
        });
    }    

    //operation = MOVE
    public Route move(Request t) {        
        /*
            support to move file/directory in another existing directory
        */
        
        return parameter("target", target -> {
            final AbsolutePath targetPath = AbsolutePath.of(target);
            final AssetType targetType = getStorage().typeOf(t.getContainer(), targetPath);
            
            if(AssetType.FILE == targetType) {
                return complete(StatusCodes.BAD_REQUEST, "target must be a directory");
            }
            
            final AssetType currentType = getStorage().typeOf(t.getContainer(), AbsolutePath.of(t.getPath()));
            
            if (AssetType.FOLDER == currentType && ! StringUtils.startsWith(t.getPath(), targetPath.getPath())) {
                return complete(StatusCodes.BAD_REQUEST, "target cannot start with currentPath");
            }
            
            return withFuture(() -> {
                final String result = getStorage().move(t.getContainer(), t.getPath(), targetPath);

                return getStorage().listStatus(t.getContainer(), result);
            });
        });                
    }
    
}
