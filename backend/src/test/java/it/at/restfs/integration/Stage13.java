package it.at.restfs.integration;

import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage13 extends BaseTest {
    
    /*
        non è possibile la rinomina di una directory in una che esiste già
     */
    
    @Test
    public void simpleCase() {    
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(), 
                buildCommand("dir", Operation.MKDIRS),
                buildCommand("dir2", Operation.MKDIRS),
                buildCommand("", Operation.LISTSTATUS),
                buildCommand("dir", Operation.RENAME, queryBuilder("target", "dir2"))
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=409, message=Conflict, url=http://localhost:8081/restfs/v1/dir?op=RENAME&target=dir2}", 
            r.getResponse().toString()
        );        
    }
    
}