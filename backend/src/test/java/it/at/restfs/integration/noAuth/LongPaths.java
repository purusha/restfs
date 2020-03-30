package it.at.restfs.integration.noAuth;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.text.RandomStringGenerator;
import org.apache.commons.text.RandomStringGenerator.Builder;
import org.junit.Test;

import com.google.common.collect.Lists;

import it.at.restfs.NoAuthBaseTest;
import it.at.restfs.Operation;

public class LongPaths extends NoAuthBaseTest {
	
	private static final Builder BUILDER = new RandomStringGenerator.Builder();
	private static final Random RANDOM = new Random();
	
    @Test
    public void simpleCase() throws Throwable {
        
        final ExecutionContext ctx = context();
        
        final List<String> folders = createFolders(ctx, 120);
        
        createFiles(ctx, 250, folders);
        
    }
    
    //XXX duplicated from it.at.restfs.integration.noAuth.RandomGenerator
    private List<String> createFolders(ExecutionContext ctx, int numberOf) {        
        final ExecutionCommand[] command = new ExecutionCommand[numberOf];
        final List<String> result = Lists.newArrayList();
        final RandomStringGenerator gen = BUILDER.withinRange('a', 'z').build();
        
        for (int i = 0; i < numberOf; i++) {
            final String path = IntStream.range(1, RANDOM.nextInt(15) + 5)
                .mapToObj(x -> gen.generate(RANDOM.nextInt(15) + 5))                
                .collect(Collectors.joining("/"));

            result.add(path);
            command[i] = buildCommand(path, Operation.MKDIRS);
        }
        
        runCommands(ctx, command);
        
        return result;
    }
    
    //XXX duplicated from it.at.restfs.integration.noAuth.RandomGenerator
    private List<String> createFiles(ExecutionContext ctx, int numberOf, List<String> folder) {
        final ExecutionCommand[] command = new ExecutionCommand[numberOf];
        final List<String> result = Lists.newArrayList();
        final RandomStringGenerator gen = BUILDER.withinRange('a', 'z').build();
        
        for (int i = 0; i < numberOf; i++) {
            final String fileName = gen.generate(RANDOM.nextInt(5) + 5);
            final String folderName = folder.get(RANDOM.nextInt(folder.size()));
            final String path = folderName + "/" + fileName;
            
            result.add(path);
            command[i] = buildCommand(path, Operation.CREATE);
        }
        
        runCommands(ctx, command);
        
        return result;
    }    
}
