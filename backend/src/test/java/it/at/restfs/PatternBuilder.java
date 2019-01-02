package it.at.restfs;

public class PatternBuilder {

    private static final String DT = ":\"[0-9]{4}-[0-9]{2}-[0-9]{2}@[0-9]{2}:[0-9]{2}:[0-9]{2}\"";    
    private static final String J_S = "\\{";    
    private static final String J_E = "\\}";

    public static String file(String f) {
        return J_S + "\"created\"" + DT + ",\"lastAccess\"" + DT + ",\"length\":0,\"modified\"" + DT + ",\"name\":\"" + f + "\",\"type\":\"FILE\"" + J_E;        
    }
    
    public static String folder(String f) {
        return J_S + "\"children\":\\[\\],\"created\"" + DT + ",\"lastAccess\"" + DT + ",\"length\":0,\"modified\"" + DT + ",\"name\":\"" + f + "\",\"type\":\"FOLDER\"" + J_E;
    }
    
}
