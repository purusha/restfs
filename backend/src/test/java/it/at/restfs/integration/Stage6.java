package it.at.restfs.integration;

import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage6 extends BaseTest {  
  
    @Test
    public void simpleCase() {      
        runCommands(
            ExecutionContext.builder()
                .container(getContainer())
                .stopOnError(true)
                .build(),
            buildCommand("file", Operation.CREATE),
            buildCommand("file", Operation.APPEND, "my body")
        );       
    }    
    
}