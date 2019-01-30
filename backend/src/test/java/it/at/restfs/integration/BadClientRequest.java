package it.at.restfs.integration;

import static java.nio.charset.Charset.defaultCharset;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Stage;
import it.at.restfs.http.PathResolver;

public class BadClientRequest extends BaseTest {

    @Test
    public void requestWithoutAcceptHeader() throws Exception {
        final String out = curl(false, true, true, true);        
                
        matchEverywhere("< HTTP/1.1 400 Bad Request", out); 
    }
    
    @Test
    public void requestWithoutAuthorizationHeader() throws Exception {
        final String out = curl(true, false, true, true);        
        
        matchEverywhere("< HTTP/1.1 400 Bad Request", out);                 
    }

    @Test
    public void requestWithoutXContainerHeader() throws Exception {
        final String out = curl(true, true, false, true);        
        
        matchEverywhere("< HTTP/1.1 400 Bad Request", out);                 
    }

    @Test
    public void requestWithoutOpParameter() throws Exception {
        final String out = curl(true, true, true, false);        
        
        matchEverywhere("< HTTP/1.1 404 Not Found", out); //see https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/parameter-directives/parameter.html             
    }
    
    //XXX please don't reause this
    private String curl(boolean accept, boolean authorization, boolean container, boolean op) throws IOException, InterruptedException {
        final List<String> curlParams = new ArrayList<String>();
        
        curlParams.add("/usr/bin/curl");    
        curlParams.add("-v");
        curlParams.add("-s");
        
        if (accept) {
            curlParams.add("-H");
            curlParams.add("Accept: application/json");            
        }
        
        if (authorization) {
            curlParams.add("-H");
            curlParams.add("Authorization: " + Stage._42);
        }
        
        if (container) {
            curlParams.add("-H");
            curlParams.add("X-Container: " + getContainer().toString());
        }
        
        curlParams.add("-H");
        curlParams.add("Content-Encoding: identity");
        
        curlParams.add("-X");
        curlParams.add("POST");
        
        final String uri;
        
        if (op) {
            uri = String.format("http://%s:%d/%s/%s/file?op=CREATE", getPublicEndpoint().getKey(), getPublicEndpoint().getValue(), PathResolver.APP_NAME, PathResolver.VERSION);
        } else {
            uri = String.format("http://%s:%d/%s/%s/file", getPublicEndpoint().getKey(), getPublicEndpoint().getValue(), PathResolver.APP_NAME, PathResolver.VERSION);
        }
        
        curlParams.add(uri);        
        
        final ProcessBuilder pb = new ProcessBuilder(curlParams);
        pb.redirectErrorStream(true);
        
        final Process process = pb.start();

        final String out = String.join(
            "\n", 
            IOUtils.readLines(
                process.getInputStream(), defaultCharset()
            )
        ); 
        
        if (process.waitFor() == 0) {
            System.out.println("curl: Success!\n");
        } else {
            System.out.println("curl: Failure!\n");            
        }
        
        return out;
    }
    
}
