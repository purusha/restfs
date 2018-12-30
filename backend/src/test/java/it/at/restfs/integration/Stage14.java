package it.at.restfs.integration;

import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage14 extends BaseTest {

    /*
        non è possibile la rinomina di una directory che non esiste
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
                buildCommand("dir", Operation.CREATE),
                buildCommand("", Operation.LISTSTATUS),
                buildCommand("dir2", Operation.RENAME, queryBuilder("target", "dir3"))
            );   
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=404, message=Not Found, url=http://localhost:8081/restfs/v1/dir2?op=RENAME&target=dir3}",
            r.getResponse().toString()
        );                
    }    
}