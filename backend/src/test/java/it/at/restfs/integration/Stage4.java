package it.at.restfs.integration;

import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage4 extends BaseTest {  
  
    @SuppressWarnings("unchecked")
    @Test
    public void simpleCase() {      
        runCommands(
            ExecutionContext.builder()
                .container(getContainer())
                .stopOnError(true)
                .build(),
            buildCommand("file1", Operation.CREATE),
            buildCommand("file2", Operation.CREATE),
            buildCommand("dir/dir2", Operation.MKDIRS),               
            buildCommand("file1", Operation.MOVE, queryBuilder("target", "dir/dir2")),
            buildCommand("file2", Operation.MOVE, queryBuilder("target", "dir"))
        );       
    }    
    
}