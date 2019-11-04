package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.parameter;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import it.at.restfs.http.services.PathHelper.Request;
import it.at.restfs.http.services.PerRequestContext;
import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PutController implements Controller {

	private PerRequestContext x;            

    //operation = RENAME
    public Route rename(Request t) {        
        /*
             rename file o folder
         */
        
        return parameter("target", target -> {
            if(StringUtils.indexOfAny(target, "\\/") >= 0) { //XXX target non può essere un path
                return complete(StatusCodes.BAD_REQUEST, "target cannot be a directory");
            }
            
            return x.withFuture(() -> {
                final String result = x.getStorage().rename(t.getPath(), target);
                final AssetType typeOf = x.getStorage().typeOf(AbsolutePath.of(result));
                
                return AssetType.FILE == typeOf ? 
            		x.getStorage().getStatus(result) :
        			x.getStorage().listStatus(result);                    
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
            final AssetType targetType = x.getStorage().typeOf(targetPath);
            
            if(AssetType.FILE == targetType) {
                return complete(StatusCodes.BAD_REQUEST, "target must be a directory");
            }
            
            final AssetType currentType = x.getStorage().typeOf(AbsolutePath.of(t.getPath()));
            
            if (AssetType.FOLDER == currentType && ! StringUtils.startsWith(t.getPath(), targetPath.getPath())) {
                return complete(StatusCodes.BAD_REQUEST, "target cannot start with currentPath");
            }
            
            return x.withFuture(() -> {
                final String result = x.getStorage().move(t.getPath(), targetPath);

                return x.getStorage().listStatus(result);
            });
        });                
    }
    
}
