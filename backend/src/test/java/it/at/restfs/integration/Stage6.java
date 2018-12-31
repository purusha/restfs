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
                .printResponse(true)
                .build(),
            buildCommand("file", Operation.CREATE),
            buildCommand("file", Operation.APPEND, "my body"),
            buildCommand("file", Operation.OPEN)
        );       
    }    
    
    /*
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
    */
    
    @Test
    public void appendOnNotExistingFile() {    
        NotSuccessfullResult r = null;
        
        try {
                  
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .printResponse(true)
                    .build(),
                buildCommand("file2", Operation.APPEND, "my body")
            );   
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
            
        expected(
            "Response{protocol=http/1.1, code=404, message=Not Found, url=http://localhost:8081/restfs/v1/file2?op=APPEND}", 
            r.getResponse().toString()
        );        
    }    

    @Test
    public void openOnNotExistingFile() {      
        NotSuccessfullResult r = null;
        
        try {                

            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .printResponse(true)
                    .build(),
                buildCommand("file3", Operation.OPEN)
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
            
        expected(
            "Response{protocol=http/1.1, code=404, message=Not Found, url=http://localhost:8081/restfs/v1/file3?op=OPEN}", 
            r.getResponse().toString()
        );        
    }    
    
}