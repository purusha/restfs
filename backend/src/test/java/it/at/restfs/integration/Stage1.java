package it.at.restfs.integration;

import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage1 extends BaseTest {
    
    @Test
    public void simpleCase() {
        runCommands(
            ExecutionContext.builder()
                .container(getContainer())
                .stopOnError(true)
                .build(), 
            buildCommand("file", Operation.CREATE),
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS),
            buildCommand("file", Operation.RENAME, queryBuilder("target", "file2")),
            buildCommand("dir", Operation.RENAME, queryBuilder("target", "dir2")),
            buildCommand("file2", Operation.GETSTATUS),
            buildCommand("dir2", Operation.LISTSTATUS)            
        );
    }

}
