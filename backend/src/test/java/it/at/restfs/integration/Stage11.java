package it.at.restfs.integration;

import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage11 extends BaseTest {
    
    /*
        non è possibile la rinomina di un file in uno che esiste già
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
                buildCommand("file2", Operation.CREATE),
                buildCommand("", Operation.LISTSTATUS),
                buildCommand("file", Operation.RENAME, queryBuilder("target", "file2"))
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=409, message=Conflict, url=http://localhost:8081/restfs/v1/file?op=RENAME&target=file2}", 
            r.getResponse().toString()
        );        
    }

}
