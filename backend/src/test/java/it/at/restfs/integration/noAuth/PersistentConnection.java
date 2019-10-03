package it.at.restfs.integration.noAuth;

import static java.nio.charset.Charset.defaultCharset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import it.at.restfs.NoAuthBaseTest;
import it.at.restfs.Operation;
import it.at.restfs.http.services.PathHelper;

public class PersistentConnection extends NoAuthBaseTest {  
    
    /*

        see https://en.wikipedia.org/wiki/HTTP_persistent_connection for detail 
        
     */
    
    @Test
    public void simpleCase() throws Exception {            	
        final String response = doubleCall();
        
        matchEverywhere(String.format("Re-using existing connection! (#0) with host %s", getPublicEndpoint().getKey()) , response);
        
    }

    //XXX please make it with HttpClient instead of this !!?
    private String doubleCall() throws IOException, InterruptedException {
        final List<String> curlParams = new ArrayList<String>();
        
        curlParams.add("/usr/bin/curl");    
        curlParams.add("-v");
        curlParams.add("-s");
        curlParams.add("-H");
        curlParams.add("Accept: application/json");
//        curlParams.add("-H");
//        curlParams.add("Authorization: ");
        curlParams.add("-H");
        curlParams.add("X-Container: " + getContainer().toString());
        curlParams.add("-H");
        curlParams.add("Content-Encoding: gzip");
        curlParams.add("-X");
        curlParams.add("POST");
        
        curlParams.add(String.format(
            "http://%s:%d/%s/%s/file1?op=%s", getPublicEndpoint().getKey(), getPublicEndpoint().getValue(), PathHelper.APP_NAME, PathHelper.VERSION, Operation.CREATE.name()                    
        ));
        
        curlParams.add(String.format(
            "http://%s:%d/%s/%s/file2?op=%s", getPublicEndpoint().getKey(), getPublicEndpoint().getValue(), PathHelper.APP_NAME, PathHelper.VERSION, Operation.CREATE.name()                    
        ));        
        
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
