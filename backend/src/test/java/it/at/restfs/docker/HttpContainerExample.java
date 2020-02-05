package it.at.restfs.docker;

import static java.nio.charset.Charset.defaultCharset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class HttpContainerExample {
	
	/*
	 	$> docker run -p 8080:80 --rm -t mendhak/http-https-echo
	 */
	
	@ClassRule
	public static DockerComposeContainer<?> container = new DockerComposeContainer<>(new File("src/test/resources/simple-http/docker-compose.yml"))
		.withExposedService("my-http-listener_1", 8080, Wait.forHttp("/test").forStatusCode(200));
	
	
	@Test
	public void callHttp() throws Exception {
		final String out = realCall("http://localhost:8080/me");
		
		System.out.println(out);
	}
	
	
	private String realCall(String url) throws Exception {		
        final List<String> curlParams = new ArrayList<String>();
        
        curlParams.add("/usr/bin/curl");
        curlParams.add("-v");
        curlParams.add("-s");        
        curlParams.add(url);
        
        final ProcessBuilder pb = new ProcessBuilder(curlParams);
        pb.redirectErrorStream(true);
        
        final Process process = pb.start();

        final String out = String.join(
            "\n", 
            IOUtils.readLines(
                process.getInputStream(), defaultCharset()
            )
        ); 
        
        if (process.waitFor() != 0) {
            throw new RuntimeException("curl: Failure!\n");
        }        
        
		return out;
	}
	

}
