package it.at.restfs;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
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
    
}
