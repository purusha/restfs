package it.at.restfs.integration.masterPwd;

import org.junit.Test;

import it.at.restfs.MasterPwdBaseTest;
import it.at.restfs.Operation;

public class Stage5 extends MasterPwdBaseTest {  
  
    @Test
    public void simpleCase() {      
        runCommands(
    		context(),        		
            buildCommand("dir/dir2/dir3/dir4/dir5", Operation.MKDIRS),               
            buildCommand("dir/dir2/dir3/dir4", Operation.MOVE, queryBuilder("target", "dir/dir2")),
            buildCommand("dir/dir2/dir3", Operation.MOVE, queryBuilder("target", "dir"))
        );       
    }

    @Test
    public void simpleCaseWithTargetStartsWithSlash() {      
        runCommands(
    		context(),        		
            buildCommand("dir/dir2/dir3/dir4/dir5", Operation.MKDIRS),               
            buildCommand("dir/dir2/dir3/dir4", Operation.MOVE, queryBuilder("target", "/dir/dir2")),
            buildCommand("dir/dir2/dir3", Operation.MOVE, queryBuilder("target", "/dir"))
        );       
    }
    
}
