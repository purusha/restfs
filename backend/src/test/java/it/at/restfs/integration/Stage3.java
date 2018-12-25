package it.at.restfs.integration;

import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage3 extends BaseTest {  
  
    /*
        operations on file/directory that does not exist
     */

    @SuppressWarnings("unchecked")
    @Test
    public void simpleCase() {          
        runCommands(
            ExecutionContext.builder()
                .container(getContainer())
                .stopOnError(false)
                .build(), 
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS),
            buildCommand("file", Operation.RENAME, queryBuilder("target", "file2")),
            buildCommand("dir", Operation.RENAME, queryBuilder("target", "dir2")),
            buildCommand("file", Operation.DELETE),
            buildCommand("dir", Operation.DELETE)
        ); 
        
        //XXX how to verify at least all http response status !!?
    }    
}