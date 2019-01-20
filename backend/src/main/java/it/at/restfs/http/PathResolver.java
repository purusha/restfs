package it.at.restfs.http;

import org.apache.commons.lang3.StringUtils;
import akka.http.javadsl.model.Uri;
import lombok.SneakyThrows;

public class PathResolver {
    
    public static final String APP_NAME = "restfs";
    public static final String VERSION = "v1";    

    @SneakyThrows
    public static String getPathString(Uri uri) {
        final String substringAfter = StringUtils.substringAfter(uri.getPathString(), APP_NAME + "/" + VERSION);
        final String decode = java.net.URLDecoder.decode(substringAfter, "UTF-8");
        
        return decode;
    }
    
}
