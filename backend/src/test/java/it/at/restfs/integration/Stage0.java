package it.at.restfs.integration;

import org.junit.Test;

import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class Stage0 extends BaseTest {
    
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
