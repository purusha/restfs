package it.at.restfs.integration;

import static it.at.restfs.PatternBuilder.file;
import static it.at.restfs.PatternBuilder.folder;
import java.util.List;
import org.junit.Test;
import com.google.common.collect.Iterables;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;
import okhttp3.ResponseBody;

public class Stage1 extends BaseTest {
        
    @Test
    public void simpleCase() throws Exception {
        final List<ResponseBody> commands = runCommands(
            ExecutionContext.builder()
                .container(getContainer())
                .stopOnError(true)
                .build(), 
            buildCommand("file", Operation.CREATE),
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS),
            buildCommand("file", Operation.RENAME, queryBuilder("target", "file2")),
            buildCommand("dir", Operation.RENAME, queryBuilder("target", "dir2")),
            buildCommand("file2", Operation.GETSTATUS),
            buildCommand("dir2", Operation.LISTSTATUS)            
        );
        
        match(
            file("file2"), Iterables.get(commands, 6).string()
        );
        
        match(
            folder("dir2"), Iterables.get(commands, 7).string()
        );        
    }

}
