package it.at.restfs.integration;

import org.junit.Test;

import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class DontAllowedCall extends BaseTest {
    
    @Test
    public void cantRenameFileWithFileNameThatJustExist() {
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
        		context(), 
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
    
    @Test
    public void cantRenameFileThatDoesNotExist() {        
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
        		context(),             		
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
    
    @Test
    public void cantRenameFolderWithFolderNameThatJustExist() {    
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
        		context(),             		
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

    @Test
    public void cantRenameFolderThatDoesNotExist() {
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
        		context(),             		
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
