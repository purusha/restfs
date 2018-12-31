package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.parameter;
import org.apache.commons.lang3.StringUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.storage.AbsolutePath;
import it.at.restfs.storage.AssetType;
import it.at.restfs.storage.Storage;

@Singleton
public class PutController extends BaseController {

    @Inject
    public PutController(Storage storage) {
        super(storage);
    }

    //operation = RENAME
    public Route rename(Request t) {
        return parameter("target", target -> {            
            if(StringUtils.indexOfAny(target, "\\/") == -1) { //XXX target non può essere un path
                
                final String result = getStorage().rename(t.getContainer(), t.getPath(), target);
                final AssetType typeOf = getStorage().typeOf(t.getContainer(), AbsolutePath.of(result));
                final Request req = new Request(t.getContainer(), result, t.getOperation());
                
                return AssetType.FILE == typeOf ?
                    getFileStatus(req) :
                    getDirectoryStatus(req);
                                
            } else {
                return complete(StatusCodes.BAD_REQUEST, "target cannot be a directory");
            }
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
          
            final String result = getStorage().move(t.getContainer(), t.getPath(), targetPath);
            final Request req = new Request(t.getContainer(), result, t.getOperation());
            
            return getDirectoryStatus(req);            
        });                
    }
    
}
