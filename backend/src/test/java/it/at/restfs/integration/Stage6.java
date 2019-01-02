package it.at.restfs.integration;

import org.junit.Test;
import com.google.common.collect.Iterables;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;
import okhttp3.ResponseBody;

public class Stage6 extends BaseTest {  
  
    @Test
    public void simpleCase() throws Exception {              
        final ResponseBody open = Iterables.getLast(
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(),
                buildCommand("file", Operation.CREATE),
                buildCommand("file", Operation.APPEND, "my body"),
                buildCommand("file", Operation.OPEN)
            )                
        );
        
        expected(
            "{\"content\":[\"my body\"],\"path\":\"/file\"}", 
            open.string()
        );        
    }    

    @Test
    public void htmlContent() throws Exception {              
        final ResponseBody open = Iterables.getLast(
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(),
                buildCommand("file5", Operation.CREATE),
                buildCommand("file5", Operation.APPEND, "<html><head></head><body><p>Hello World</p></body></html>"),
                buildCommand("file5", Operation.OPEN)
            )                
        );
        
        expected(
            "{\"content\":[\"<html><head></head><body><p>Hello World</p></body></html>\"],\"path\":\"/file5\"}", 
            open.string()
        );        
    }    
    
    @Test
    public void moreAppendWithDifferenteCarriageReturn() throws Exception {              
        final ResponseBody open = Iterables.getLast(
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(),
                buildCommand("file4", Operation.CREATE),
                buildCommand("file4", Operation.APPEND, "my body"),
                buildCommand("file4", Operation.APPEND, "\n"),
                buildCommand("file4", Operation.APPEND, "my body"),
                buildCommand("file4", Operation.APPEND, "\r"),
                buildCommand("file4", Operation.APPEND, "my body"),
                buildCommand("file4", Operation.APPEND, "\r\n"),
                buildCommand("file4", Operation.APPEND, "my body"),
                buildCommand("file4", Operation.OPEN)
            )                
        );
        
        expected(
            "{\"content\":[\"my body\",\"my body\",\"my body\",\"my body\"],\"path\":\"/file4\"}", 
            open.string()
        );        
    }    
    
    @Test
    public void downloadDirectoryIsNotAllowed() {      
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
                    .build(),
                buildCommand("dir", Operation.MKDIRS),
                buildCommand("dir", Operation.OPEN)
            );       
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
            
        expected(
            "Response{protocol=http/1.1, code=400, message=Bad Request, url=http://localhost:8081/restfs/v1/dir?op=OPEN}", 
            r.getResponse().toString()
        );                    
    }    

    @Test
    public void appendOnNotExistingFile() {    
        NotSuccessfullResult r = null;
        
        try {
                  
            runCommands(
                ExecutionContext.builder()
                    .container(getContainer())
                    .stopOnError(true)
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