package it.at.restfs.integration.noAuth;

import org.junit.Test;

import it.at.restfs.NoAuthBaseTest;
import it.at.restfs.Operation;

public class Stage0 extends NoAuthBaseTest {
    
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
