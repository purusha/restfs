package it.at.restfs;

import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import lombok.Getter;

public class BaseTest extends Stage {

    @Getter
    private final UUID container = UUID.randomUUID();    
    
    @Before
    public void setUp() {
        System.out.println("\nprocess " + this.getClass().getSimpleName() + " on " + container);
        
        createContainer(container);                
    }
    
    @After
    public void tearDown() {
        showDiff(container);                
    }

}
