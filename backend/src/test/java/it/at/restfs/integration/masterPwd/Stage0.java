package it.at.restfs.integration.masterPwd;

import org.junit.Test;

import it.at.restfs.MasterPwdBaseTest;
import it.at.restfs.Operation;

public class Stage0 extends MasterPwdBaseTest {
    
    @Test
    public void simpleCase() {
        runCommands(
    		context(), 
            buildCommand("file", Operation.CREATE),
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS)
        );
    }

}
