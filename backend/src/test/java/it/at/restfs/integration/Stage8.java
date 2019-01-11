package it.at.restfs.integration;

import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage8 extends BaseTest {  
  
    @Test
    public void nameWithSpecialChars() throws Exception {      
        runCommands(
            ExecutionContext.builder()
                .container(getContainer())
                .printResponse(true)
                .stopOnError(true)
                .build(),
            buildCommand("dir/dir#", Operation.MKDIRS),               
            buildCommand("dir/dir&", Operation.MKDIRS),
            buildCommand("dir/dir@", Operation.MKDIRS),
            buildCommand("dir/dir$", Operation.MKDIRS),
            buildCommand("dir/dir_", Operation.MKDIRS),
            buildCommand("dir/with space", Operation.MKDIRS)
        );
        
//        match(
//            folder("dir%23"), Iterables.get(commands, 0).string()
//        );
        
//        match(
//            folder("with%20space"), Iterables.get(commands, 1).string()
//        );                
    }
    
}
