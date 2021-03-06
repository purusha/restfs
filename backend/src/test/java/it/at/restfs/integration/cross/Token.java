package it.at.restfs.integration.cross;

import static java.nio.charset.Charset.defaultCharset;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;

import it.at.restfs.IntegrationResolver;
import it.at.restfs.MasterPwdBaseTest;
import it.at.restfs.NoAuthBaseTest;
import it.at.restfs.OSFeatures;
import it.at.restfs.auth.AuthorizationChecker.Implementation;
import it.at.restfs.http.services.PathHelper;

public class Token {
	
	private static final IntegrationResolver RESOLVER = 
		new IntegrationResolver();
	
	private static final RandomStringGenerator BUILDER = 
		new RandomStringGenerator.Builder().filteredBy(CharacterPredicates.ASCII_ALPHA_NUMERALS).build();
		
	/*
	 * this test must be provided for each type of values inner ==> it.at.restfs.auth.AuthorizationChecker
	 */
	
	public static class NoAuth extends NoAuthBaseTest {		
		
	    @Test
	    public void tokenWithEmptyAuth() throws Exception {            	
	        final String response = callToken(getContainer(), Optional.empty());
	        
	        matchEverywhere("HTTP/1.1 405 Method Not Allowed" , response);
	    }	    
	}
	
	public static class MasterPwd extends MasterPwdBaseTest {
		
	    @Test
	    public void tokenWithCorrectAuth() throws Exception {            	
	        final String response = callToken(getContainer(), Optional.of(getFirstPassword()));
	        
	        matchEverywhere("\"type\":\"" + Implementation.MASTER_PWD.name() + "\",\"ttl\"" , response);	        	       
	    }
	    
	    @Test
	    public void tokenWithWrongAuth() throws Exception {        
	        final String response = callToken(
        		getContainer(), 
        		Optional.of(BUILDER.generate(8))
    		);
	        
	        matchEverywhere("HTTP/1.1 403 Forbidden" , response);	        
	    }		

	    @Test
	    public void tokenWithEmptyAuth() throws Exception {        
	        final String response = callToken(
        		getContainer(), Optional.of(StringUtils.EMPTY)
    		);
	        
	        matchEverywhere("HTTP/1.1 403 Forbidden" , response);	        
	    }			    
	}
	
    //XXX please make it with HttpClient instead of this !!?
    private static String callToken(UUID containerId, Optional<String> authHeader) throws IOException, InterruptedException {
    	final List<String> curlParams = getFeatures().curl();

    	curlParams.add("-H");
        curlParams.add("Accept: application/json");
        
        if (authHeader.isPresent()) {
			curlParams.add("-H");
			curlParams.add("Authorization: " + authHeader.get());        	
        }
        
        curlParams.add("-H");
        curlParams.add("X-Container: " + containerId.toString());
        curlParams.add("-H");
        curlParams.add("Content-Encoding: gzip");
        curlParams.add("-X");
        curlParams.add("POST");
        
        curlParams.add(String.format(
            "http://%s:%d/%s/%s/token", getPublicEndpoint().getKey(), getPublicEndpoint().getValue(), PathHelper.APP_NAME, PathHelper.VERSION                    
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
        
        if (process.waitFor() != 0) {
            throw new RuntimeException("curl: Failure!\n");
        }        
        
        return out;
    }   
    
    private static OSFeatures getFeatures() {
		return RESOLVER.getFeatures();
	}

    private static Pair<String, Integer> getPublicEndpoint() {
    	return RESOLVER.getPublicEndpoint();
    }
	
}
