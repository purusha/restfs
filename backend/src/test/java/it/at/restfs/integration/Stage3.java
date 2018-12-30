package it.at.restfs.integration;

import org.junit.Ignore;
import org.junit.Test;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage3 extends BaseTest {  
  
    @Test
    public void fileGetStatus() {             
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(),
                buildCommand("file", Operation.GETSTATUS)                    
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=404, message=Not Found, url=http://localhost:8081/restfs/v1/file?op=GETSTATUS}", 
            r.getResponse().toString()
        );                
    }
    
    @Test
    public void dirListStatus() {             
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(),
                buildCommand("dir", Operation.LISTSTATUS)                    
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=404, message=Not Found, url=http://localhost:8081/restfs/v1/dir?op=LISTSTATUS}", 
            r.getResponse().toString()
        );                
    }    
    
    @Test
    public void rename() {             
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(),
                buildCommand("file", Operation.RENAME, queryBuilder("target", "file2"))
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=404, message=Not Found, url=http://localhost:8081/restfs/v1/file?op=RENAME&target=file2}", 
            r.getResponse().toString()
        );                
    }
    
    @Test
    public void delete() {             
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(),
                buildCommand("file", Operation.DELETE)
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=404, message=Not Found, url=http://localhost:8081/restfs/v1/file?op=DELETE}", 
            r.getResponse().toString()
        );                
    }    
    
    @Test
    public void create() {             
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(),
                buildCommand("dir/file", Operation.CREATE)
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=404, message=Not Found, url=http://localhost:8081/restfs/v1/dir/file?op=CREATE}", 
            r.getResponse().toString()
        );                
    }    
    
    @Test
    public void createDoubleFile() {             
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(),
                buildCommand("file", Operation.CREATE),
                buildCommand("file", Operation.CREATE)
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=409, message=Conflict, url=http://localhost:8081/restfs/v1/file?op=CREATE}", 
            r.getResponse().toString()
        );                
    }    

    @Ignore
    @Test
    public void createDoubleFolder() {             
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(),
                buildCommand("dir", Operation.MKDIRS),
                buildCommand("dir", Operation.MKDIRS)
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=409, message=Conflict, url=http://localhost:8081/restfs/v1/dir?op=MKDIRS}", 
            r.getResponse().toString()
        );                
    }    
    
}