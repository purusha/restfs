package it.at.restfs.integration.noAuth;

import static it.at.restfs.PatternBuilder.json;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Iterables;

import it.at.restfs.NoAuthBaseTest;
import it.at.restfs.Operation;
import it.at.restfs.event.EventView;
import it.at.restfs.event.ShortTimeInMemory;
import okhttp3.ResponseBody;

public class Statistics extends NoAuthBaseTest {
	
	private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @Test
    public void simpleCase() throws Throwable {
        
        final ExecutionContext ctx = context();
        
        runCommands(
            ctx, 
            buildCommand("fileX", Operation.CREATE),
            buildCommand("dirX", Operation.MKDIRS),
            buildCommand("fileX", Operation.GETSTATUS),
            buildCommand("dirX", Operation.LISTSTATUS)
        );

        wait(5);
        
        final List<ResponseBody> r1 = runCommands(
            ctx, buildMgmtCommand(Operation.STATS)
        );
        
        match(json(), Iterables.get(r1, 0).string());        

        runCommands(
            ctx,
            buildCommand("file", Operation.CREATE),
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS),
            buildCommand("file", Operation.RENAME, queryBuilder("target", "file2")),
            buildCommand("dir", Operation.RENAME, queryBuilder("target", "dir2")),
            buildCommand("file2", Operation.GETSTATUS),
            buildCommand("dir2", Operation.LISTSTATUS)                        
        );
        
        wait(10);
        
        final List<ResponseBody> r2 = runCommands(
            ctx, buildMgmtCommand(Operation.STATS)
        );
        
        expected("{\"200\":13}", Iterables.get(r2, 0).string());
        
        wait(10);

        final List<ResponseBody> r3 = runCommands(
            ctx, buildMgmtCommand(Operation.LAST)
        );
        
        for (ResponseBody c : r3) {
        	System.out.println(c.string());
		}
        
    }
    
    @Test
    public void testCorrectOrderOfLastCall() throws Exception {
    	
        final ExecutionContext ctx = context();
                        
        for (int i = 1; i <= 35; i++) {        	        	
        	runCommands(ctx, buildCommand("file" + i, Operation.CREATE));
        	
        	if (i % 5 == 0) { //XXX questo influenza l'ordine nel risultato
        		wait(ShortTimeInMemory.expireData());
        	}        	
        }  
        
        wait(10);
        
        final String lastCalls = runCommands(
            ctx, buildMgmtCommand(Operation.LAST)
        ).get(0).string();                      
        
        final String paths = mapper.<List<EventView>>readValue(lastCalls, new TypeReference<List<EventView>>() { }).stream()
    		.map(e -> e.getRequest().getPath())
    		.collect(Collectors.joining(","));
        			
        Assert.assertEquals(
    		"/file31,/file32,/file33,/file34,/file35,"
    		+ "/file26,/file27,/file28,/file29,/file30,"
    		+ "/file21,/file22,/file23,/file24,/file25,"
    		+ "/file16,/file17,/file18,/file19,/file20,"
    		+ "/file11,/file12,/file13,/file14,/file15,"
    		+ "/file6,/file7,/file8,/file9,/file10", paths);
    }
    
}
