package it.at.restfs.integration;

import org.junit.Test;

import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage22 extends BaseTest {  
  
    @Test
    public void simpleCase() {
        runCommands(
    		context(),
            buildCommand("dir/dir2/dir4/dir5/dir6", Operation.MKDIRS),
            buildCommand("dir/dir2/dir4", Operation.RENAME, queryBuilder("target", "dirX")),
            buildCommand("dir/dir2/dirX/file1.html", Operation.CREATE),
            buildCommand("dir/dir2/dirX/file1.html", Operation.RENAME, queryBuilder("target", "fileX.html"))
        );
    }    
    
}