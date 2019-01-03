package it.at.restfs;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PatternBuilder {

    private static final String DT = ":\"[0-9]{4}-[0-9]{2}-[0-9]{2}@[0-9]{2}:[0-9]{2}:[0-9]{2}\"";    
    private static final String J_S = "\\{";    
    private static final String J_E = "\\}";

    public static String file(String f) {
        return J_S + "\"created\"" + DT + ",\"lastAccess\"" + DT + ",\"length\":0,\"modified\"" + DT + ",\"name\":\"" + f + "\",\"type\":\"FILE\"" + J_E;        
    }
    
    public static String folder(String f, String... children) {
        final String childrenValue = Arrays.stream(children).collect(Collectors.joining(", "));
        final Long numberOf = Arrays.stream(children).count();
        
        return 
            J_S + 
            "\"children\":\\[" + childrenValue + "\\]," + 
            "\"created\"" + DT + "," + 
            "\"lastAccess\"" + DT + "," + 
            "\"length\":" + numberOf + "," + 
            "\"modified\"" + DT + "," + 
            "\"name\":\"" + f + "\"," + 
            "\"type\":\"FOLDER\"" + J_E;
    }
    
}
