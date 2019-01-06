package it.at.restfs.integration;

import static java.nio.charset.Charset.defaultCharset;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.google.common.collect.Iterables;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;
import it.at.restfs.http.HTTPListener;
import okhttp3.ResponseBody;

public class Stage7 extends BaseTest {  
    
    private static final String TEXT = "my body"; //XXX make it random please
  
    @Test
    public void simpleCase() throws Exception {              
        final ExecutionContext context = ExecutionContext.builder()                
            .container(getContainer())
            .stopOnError(true)
            .build();
        
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
    
    //XXX please me it with HttpClient instead of this !!?
    private void appenGzipContent() throws IOException, InterruptedException {
        final List<String> curl = new ArrayList<String>();
        
        curl.add("/usr/bin/curl");    
        curl.add("-v");
        curl.add("-s");
        curl.add("--trace-ascii");
        curl.add("http_trace.log");
        curl.add("--data-binary");
        curl.add("@file.gz");
        curl.add("-H");
        curl.add("Accept: application/json");
        curl.add("-H");
        curl.add("Authorization: 42");
        curl.add("-H");
        curl.add("X-Container:" + getContainer().toString());
        curl.add("-H");
        curl.add("Content-Encoding: gzip");
        curl.add("-X");
        curl.add("POST");
        curl.add(String.format(
            "http://%s:%d/%s/%s/file?op=APPEND", HTTPListener.HOST, HTTPListener.PORT, HTTPListener.APP_NAME, HTTPListener.VERSION                    
        ));        
        
        final ProcessBuilder pb = new ProcessBuilder(curl);
        pb.directory(new File("test-gzipped/"));
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
            System.out.println(out);
        }        
    }    
       
}