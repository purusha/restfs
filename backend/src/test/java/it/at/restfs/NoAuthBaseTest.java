package it.at.restfs;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;

import it.at.restfs.auth.AuthorizationChecker.Implementation;
import lombok.Getter;
import lombok.experimental.Delegate;

public class NoAuthBaseTest extends Stage {	
	
	@Delegate
	private ExpectationsBuilder expects = new ExpectationsBuilder();
	
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
            .type(Implementation.NO_AUTH)
            .build(); 
		
		return ctx;		
	}
	
    @Before
    public void setUp() {
    	container = UUID.randomUUID();
    	
        System.out.println("\nprocess " + this.getClass().getSimpleName() + " on " + container);
        
        createContainer(context());         
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
