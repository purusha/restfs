package it.at.restfs.integration;

import static java.nio.charset.Charset.defaultCharset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import it.at.restfs.BaseTest;
import it.at.restfs.http.services.PathHelper;

public class BadClientRequest extends BaseTest {

    @Test
    public void requestWithoutAcceptHeader() throws Exception {
        final String out = curl(false, true, true, true);        
                
        matchEverywhere("< HTTP/1.1 400 Bad Request", out); 
    }
    
    @Ignore("deve essere usato un container con auth != NO_AUTH")    
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
        
        curlParams.add("/usr/bin/curl");  //XXX this is not a good idea when run in win32 machine !!?  
        curlParams.add("-v");
        curlParams.add("-s");
        
        if (accept) {
            curlParams.add("-H");
            curlParams.add("Accept: application/json");            
        }
        
        /*
        if (authorization) {
            curlParams.add("-H");
            curlParams.add("Authorization: ");
        }
        */
        
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
            uri = String.format("http://%s:%d/%s/%s/file?op=CREATE", getPublicEndpoint().getKey(), getPublicEndpoint().getValue(), PathHelper.APP_NAME, PathHelper.VERSION);
        } else {
            uri = String.format("http://%s:%d/%s/%s/file", getPublicEndpoint().getKey(), getPublicEndpoint().getValue(), PathHelper.APP_NAME, PathHelper.VERSION);
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
        
        System.out.println(out);
        
        if (process.waitFor() == 0) {
            System.out.println("curl: Success!\n");
        } else {
            System.out.println("curl: Failure!\n");                        
        }
        
        return out;
    }
    
}
