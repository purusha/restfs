package it.at.restfs.integration;

import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage12 extends BaseTest {

    /*
        non è possibile la rinomina di un file che non esiste
    */
    
    @SuppressWarnings("unchecked")
    @Test
    public void simpleCase() {        
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(), 
                buildCommand("file", Operation.CREATE),
                buildCommand("", Operation.LISTSTATUS),
                buildCommand("file2", Operation.RENAME, queryBuilder("target", "file3"))
            );   
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=404, message=Not Found, url=http://localhost:8081/restfs/v1/file2?op=RENAME&target=file3}", 
            r.getResponse().toString()
        );                
    }    

}
