package it.at.restfs.integration;

import static it.at.restfs.PatternBuilder.file;
import static it.at.restfs.PatternBuilder.folder;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Iterables;

import it.at.restfs.BaseTest;
import it.at.restfs.Operation;
import okhttp3.ResponseBody;

public class CreateFileAndFolderWithStrangeCharacter extends BaseTest {  
    
    /*
        crea folder e file con nomi contenenti caratteri speciali
        verificando il json di risposta ed il fs !!?
     */
  
    @Test
    public void folderNameWithSpecialChars() throws Exception {      
        final List<ResponseBody> commands = runCommands(
    		context(),
            buildCommand("dir/dir#", Operation.MKDIRS),               
            buildCommand("dir/dir&", Operation.MKDIRS),
            buildCommand("dir/dir@", Operation.MKDIRS),
            buildCommand("dir/dir$", Operation.MKDIRS),
            buildCommand("dir/dir_", Operation.MKDIRS),
            buildCommand("dir/with space", Operation.MKDIRS)
        );
        
        match(
            folder("dir#"), Iterables.get(commands, 0).string()
        );

        match(
            folder("dir&"), Iterables.get(commands, 1).string()
        );

        match(
            folder("dir@"), Iterables.get(commands, 2).string()
        );
        
        match(
            folder("dir$"), Iterables.get(commands, 3).string()
        );
        
        match(
            folder("dir_"), Iterables.get(commands, 4).string()
        );

        match(
            folder("with space"), Iterables.get(commands, 5).string()
        );                
    }

    @Test
    public void fileNamesWithSpecialChars() throws Exception {      
        final List<ResponseBody> commands = runCommands(
    		context(),        		
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("dir/dir#", Operation.CREATE),               
            buildCommand("dir/dir&", Operation.CREATE),
            buildCommand("dir/dir@", Operation.CREATE),
            buildCommand("dir/dir$", Operation.CREATE),
            buildCommand("dir/dir_", Operation.CREATE),
            buildCommand("dir/with space", Operation.CREATE)
        );
        
        match(
            file("dir#"), Iterables.get(commands, 1).string()
        );

        match(
            file("dir&"), Iterables.get(commands, 2).string()
        );

        match(
            file("dir@"), Iterables.get(commands, 3).string()
        );
        
        match(
            file("dir$"), Iterables.get(commands, 4).string()
        );
        
        match(
            file("dir_"), Iterables.get(commands, 5).string()
        );

        match(
            file("with space"), Iterables.get(commands, 6).string()
        );                
    }
    
}
