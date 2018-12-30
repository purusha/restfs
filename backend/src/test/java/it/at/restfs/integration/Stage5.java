package it.at.restfs.integration;

import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage5 extends BaseTest {  
  
    @Test
    public void simpleCase() {      
        runCommands(
            ExecutionContext.builder()
                .container(getContainer())
                .stopOnError(true)
                .build(),
            buildCommand("dir/dir2/dir3/dir4/dir5", Operation.MKDIRS),               
            buildCommand("dir/dir2/dir3/dir4", Operation.MOVE, queryBuilder("target", "dir/dir2")),
            buildCommand("dir/dir2/dir3", Operation.MOVE, queryBuilder("target", "dir"))
        );       
    }
    
}
