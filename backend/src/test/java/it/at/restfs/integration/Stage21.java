package it.at.restfs.integration;

import org.junit.Test;

import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage21 extends BaseTest {  
  
    @Test
    public void simpleCase() {
        runCommands(
    		context(),  
            buildCommand("dir/dir2/dir3", Operation.MKDIRS),
            buildCommand("dir/dir2/dir3/test-no-extension", Operation.CREATE),
            buildCommand("dir/dir2/dir3/test.xml", Operation.CREATE),
            buildCommand("dir/dir2/dir3/test.json", Operation.CREATE),
            buildCommand("dir/dir2/dir4/dir5", Operation.MKDIRS),
            buildCommand("dir/dir2/dir4/dir5/file1.html", Operation.CREATE),
            buildCommand("dir/dir2/dir4/dir5/file2.html", Operation.CREATE),
            buildCommand("dir/dir2/dir4/dir5/dir6", Operation.MKDIRS),
            buildCommand("dir/dir2/dir4/dir5", Operation.DELETE)            
        );
    }
    
}