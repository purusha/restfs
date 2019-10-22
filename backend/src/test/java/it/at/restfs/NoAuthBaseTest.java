package it.at.restfs;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import it.at.restfs.auth.AuthorizationChecker.Implementation;
import lombok.Getter;
import lombok.experimental.Delegate;

public class NoAuthBaseTest extends Stage {	
	
	@Rule
	public TestRule watcher = new TestWatcher() {
		protected void starting(Description description) {			
			System.out.println("Running");
			System.out.println("class: " + description.getClassName());
			System.out.println("method: " + description.getMethodName());
			System.out.println("--------------------------------------------------");			
		}
	};
	
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
//          .printResponse(true)
            .stopOnError(true)
            .type(Implementation.NO_AUTH)
            .build(); 
		
		return ctx;		
	}
	
    @Before
    public void setUp() {
    	container = UUID.randomUUID();
    	System.out.println("container: " + container);
        
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
