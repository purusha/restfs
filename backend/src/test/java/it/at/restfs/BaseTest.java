package it.at.restfs;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;

import lombok.Getter;
import lombok.SneakyThrows;

public class BaseTest extends Stage {
	
	@Getter
	private UUID container;

	/*
	 * 	di default tutti i container vengono creati auth = NO_AUTH; ecco perchè authHeader non è valorizzata
	 */
	
	public ExecutionContext context() {		
		final ExecutionContext ctx = ExecutionContext.builder()                
            .container(container)
//            .printResponse(true)
            .stopOnError(true)
//            .authHeader(" ")
            .build(); 
		
		return ctx;		
	}
	
    @Before
    public void setUp() {
    	container = UUID.randomUUID();
    	
        System.out.println("\nprocess " + this.getClass().getSimpleName() + " on " + container);
        
        createContainer(container);                
    }
    
    @After
    public void tearDown() {        
        /*
         * when run test calling remote service (on another machine)
         * this step is not available, because internally use a local file system
         * to find difference whith expected result
         */
        
        showDiff(container);        
    }
    
    protected void expected(String expected, String result) {
        if(! StringUtils.equals(expected, result)){
            
            System.err.println("expected: " + expected);
            System.err.println("result: " + result);
            
            throw new RuntimeException("Not the same !!?");
        }
    }    

    protected void match(String match, String result) {
        if(! result.matches(match)){
            
            System.err.println("match: " + match);
            System.err.println("result: " + result);
            
            throw new RuntimeException("Not the same !!?");
        }
    }
    
    protected void matchEverywhere(String match, String result) {        
        if(! StringUtils.contains(result, match)){
            
            System.err.println("match: " + match);
            System.err.println("result: " + result);
            
            throw new RuntimeException("Not the same !!?");
        }        
    }
    
    @SneakyThrows
    protected void wait(int seconds) {
        Thread.sleep(seconds * 1000);
    }
    
}
