package it.at.restfs.integration.masterPwd;

import static it.at.restfs.PatternBuilder.file;
import static it.at.restfs.PatternBuilder.folder;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Iterables;

import it.at.restfs.MasterPwdBaseTest;
import it.at.restfs.Operation;
import okhttp3.ResponseBody;

public class Stage2 extends MasterPwdBaseTest {
    
    /*

        TODO:

        in tutti i test in cui l'ExecutionContext è istanziato con l'opzione printResponse
        
        NON si possono eseguire i match(...) sulle risposte perchè la risorsa wrappata
        
        viene consumata !!?

     */
    
    @Test
    public void simpleCase() throws Exception {
        final List<ResponseBody> commands = runCommands(
    		context(), 
            buildCommand("file", Operation.CREATE),
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS),
            buildCommand("file", Operation.RENAME, queryBuilder("target", "file2")),
            buildCommand("dir", Operation.RENAME, queryBuilder("target", "dir2")),
            buildCommand("file2", Operation.DELETE),
            buildCommand("dir2", Operation.DELETE)            
        );

        match(
            file("file2"), Iterables.get(commands, 6).string()
        );
        
        match(
            folder("dir2"), Iterables.get(commands, 7).string()
        );                
    }
    
}