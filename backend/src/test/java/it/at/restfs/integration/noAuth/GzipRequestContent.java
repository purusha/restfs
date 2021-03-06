package it.at.restfs.integration.noAuth;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Iterables;

import it.at.restfs.NoAuthBaseTest;
import it.at.restfs.Operation;
import it.at.restfs.http.services.PathHelper;
import okhttp3.ResponseBody;

public class GzipRequestContent extends NoAuthBaseTest {  
    
    private static final String TEXT = "my body"; //XXX make it random please
  
    @Test
    public void simpleCase() throws Exception {  
		final ExecutionContext context = context();
    	
        runCommands(
            context,
            buildCommand("file", Operation.CREATE)
        );                

        appenGzipContent();
        
        final List<ResponseBody> commands = runCommands(
            context,
            buildCommand("file", Operation.OPEN)
        );                
                
        expected(
            "{\"content\":[\"" + TEXT + "\"],\"path\":\"/file\"}", Iterables.get(commands, 0).string()
        );        
    }
    
    //XXX please make it with HttpClient instead of this !!?
    private void appenGzipContent() throws IOException, InterruptedException {
    	final List<String> curlParams = getFeatures().curl();
        
        curlParams.add("--trace-ascii");
        curlParams.add("http_trace.log");
        curlParams.add("--data-binary");
        curlParams.add("@file.gz");
        curlParams.add("-H");
        curlParams.add("Accept: application/json");
        curlParams.add("-H");
        curlParams.add("X-Container: " + getContainer().toString());
        curlParams.add("-H");
        curlParams.add("Content-Encoding: gzip");
        curlParams.add("-X");
        curlParams.add("POST");
        curlParams.add(String.format(
            "http://%s:%d/%s/%s/file?op=APPEND", getPublicEndpoint().getKey(), getPublicEndpoint().getValue(), PathHelper.APP_NAME, PathHelper.VERSION                    
        ));        
        
        final ProcessBuilder pb = new ProcessBuilder(curlParams);
        pb.directory(new File(this.getClass().getResource("/test-gzipped").getPath()));
        pb.redirectErrorStream(true);
        
        final Process process = pb.start();
        
        if (process.waitFor() != 0) {
            throw new RuntimeException("curl: Failure!\n");
        }        
    }    
       
}