package it.at.restfs;

import java.util.Arrays;
import java.util.stream.Collectors;
import it.at.restfs.storage.AssetType;

public class PatternBuilder {

    private static final String DT = ":\"[0-9]{4}-[0-9]{2}-[0-9]{2}@[0-9]{2}:[0-9]{2}:[0-9]{2}\"";    
    private static final String J_S = "\\{";    
    private static final String J_E = "\\}";

    public static String file(String name) {        
        return file(name, 0l);
    }
    
    public static String file(String name, long length) {
        return 
            J_S + 
            "\"created\"" + DT + "," +
            "\"lastAccess\"" + DT + "," +
            "\"length\":" + length + "," + 
            "\"modified\"" + DT + "," +
            "\"name\":\"" + name + "\"," +
            "\"type\":\"" + AssetType.FILE + "\"" + 
            J_E;                
    }
    
    public static String folder(String name, String... children) {
        final String childrenValue = Arrays.stream(children).collect(Collectors.joining(", "));
        final Long length = Arrays.stream(children).count();
        
        return 
            J_S + 
            "\"children\":\\[" + childrenValue + "\\]," + 
            "\"created\"" + DT + "," + 
            "\"lastAccess\"" + DT + "," + 
            "\"length\":" + length + "," + 
            "\"modified\"" + DT + "," + 
            "\"name\":\"" + name + "\"," + 
            "\"type\":\"" + AssetType.FOLDER + "\"" + 
            J_E;
    }
    
}
