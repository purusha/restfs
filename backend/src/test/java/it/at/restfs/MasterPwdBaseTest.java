package it.at.restfs;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;

import it.at.restfs.auth.AuthorizationChecker.Implementation;
import lombok.Getter;
import lombok.experimental.Delegate;

public class MasterPwdBaseTest extends Stage {	
	
	@Delegate
	private ExpectationsBuilder expects = new ExpectationsBuilder();
	
	@Getter
	private UUID container;
	
	@Getter
	private String token;

	/*
	 * 	di default tutti i container vengono creati auth = MASTER_PWD
	 */
	
	public ExecutionContext context() {		
		final ExecutionContext ctx = ExecutionContext.builder()                
            .container(container)
//            .printResponse(true)
            .stopOnError(true)
            .type(Implementation.MASTER_PWD) //"1234567890-X"
            .authHeader(token)            
            .build(); 
		
		return ctx;		
	}
	
    @Before
    public void setUp() {
    	container = UUID.randomUUID();    	
    	
        System.out.println("\nprocess " + this.getClass().getSimpleName() + " on " + container);
        
        createContainer(context());   
        
        //XXX call token api here
        token = UUID.randomUUID().toString(); 
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
    
}
