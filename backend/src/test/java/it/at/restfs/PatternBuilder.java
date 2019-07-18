package it.at.restfs;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import it.at.restfs.storage.dto.AssetType;

public class PatternBuilder {

    private static final String DT = ":\"[0-9]{4}-[0-9]{2}-[0-9]{2}@[0-9]{2}:[0-9]{2}:[0-9]{2}\"";    
    private static final String J_S = "\\{";    
    private static final String J_E = "\\}";
    
    public static String json() {
        return J_S + ".*" + J_E;
    }

    public static String file(String name) {        
        return file(name, 0l);
    }
    
    public static String file(String name, long length) {
        final String quotedName = Pattern.quote(name);
        
        return 
            J_S + 
            "\"created\"" + DT + "," +
            "\"lastAccess\"" + DT + "," +
            "\"length\":" + length + "," + 
            "\"modified\"" + DT + "," +
            "\"name\":\"" + quotedName + "\"," +
            "\"type\":\"" + AssetType.FILE + "\"" + 
            J_E;                
    }
    
    public static String folder(String name, String... children) {
        final String childrenValue = Arrays.stream(children).collect(Collectors.joining(", "));
        final Long length = Arrays.stream(children).count();
        final String quotedName = Pattern.quote(name);
                
        return 
            J_S + 
            "\"children\":\\[" + childrenValue + "\\]," + 
            "\"created\"" + DT + "," + 
            "\"lastAccess\"" + DT + "," + 
            "\"length\":" + length + "," + 
            "\"modified\"" + DT + "," + 
            "\"name\":\"" + quotedName + "\"," + 
            "\"type\":\"" + AssetType.FOLDER + "\"" + 
            J_E;
    }
    
}
