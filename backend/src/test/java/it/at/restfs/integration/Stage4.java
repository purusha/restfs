package it.at.restfs.integration;

import java.util.List;
import org.junit.Test;
import com.google.common.collect.Iterables;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;
import okhttp3.ResponseBody;

public class Stage4 extends BaseTest {  
  
    @Test
    public void simpleCase() throws Exception {      
        final List<ResponseBody> commands = runCommands(
            ExecutionContext.builder()
                .container(getContainer())
                .stopOnError(true)
                .build(),
            buildCommand("file1", Operation.CREATE),
            buildCommand("file2", Operation.CREATE),
            buildCommand("dir/dir2", Operation.MKDIRS),               
            buildCommand("file1", Operation.MOVE, queryBuilder("target", "dir/dir2")),
            buildCommand("file2", Operation.MOVE, queryBuilder("target", "dir"))
        );    
        
        System.out.println(Iterables.get(commands, 3).string());
        
        System.out.println(Iterables.get(commands, 4).string());
    }    
    
}