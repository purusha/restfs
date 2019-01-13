package it.at.restfs.integration;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.commons.text.RandomStringGenerator.Builder;
import org.junit.Test;
import com.google.common.collect.Lists;
import it.at.restfs.BaseTest;
import it.at.restfs.Operation;

public class RandomGenerator extends BaseTest {
    
    private final Random r = new Random();
    private final Builder builder = new RandomStringGenerator.Builder();
    
    @Test
    public void a() throws Throwable {
        pipe();
    }

    @Test
    public void b() throws Throwable {
        pipe();
    }

    @Test
    public void c() throws Throwable {
        pipe();
    }

    @Test
    public void d() throws Throwable {
        pipe();
    }

    @Test
    public void e() throws Throwable {
        pipe();
    }

    @Test
    public void f() throws Throwable {
        pipe();
    }
    
    /*
            
            1) create N folder
            2) create N * N file in created folder
            3) liststatus per ogni folder
            4) getstatus per on file
            5) append, append, append on first file created
            6) download first file
            7) delete all files
            8) delete all folder
            
            wait
            
            9) call /stats
                 
     */
    
    private void pipe() {
        final ExecutionContext ctx = ExecutionContext.builder()
            .container(getContainer())
            .stopOnError(true)
            .build();
        
        final List<String> folders = createFolders(ctx, 120);
        wait(1);
        
        final List<String> files = createFiles(ctx, 250, folders);
        wait(1);
        
        get(ctx, folders, Operation.LISTSTATUS);
        wait(1);
        
        get(ctx, files, Operation.GETSTATUS);
        wait(1);
        
//        wait(5);
    }
    
    private void get(ExecutionContext ctx, List<String> resource, Operation op) {
        final int numberOf = resource.size();
        final ExecutionCommand[] command = new ExecutionCommand[numberOf];
        
        for (int i = 0; i < numberOf; i++) {
            command[i] = buildCommand(resource.get(i), op);
        }
        
        runCommands(ctx, command);
    }

    private List<String> createFolders(ExecutionContext ctx, int numberOf) {        
        final ExecutionCommand[] command = new ExecutionCommand[numberOf];
        final List<String> result = Lists.newArrayList();
        final RandomStringGenerator gen = builder.withinRange('a', 'z').build();
        
        for (int i = 0; i < numberOf; i++) {
            final String path = IntStream.range(1, r.nextInt(5) + 5)
                .mapToObj(x -> gen.generate(r.nextInt(5) + 5))                
                .collect(Collectors.joining("/"));

            result.add(path);
            command[i] = buildCommand(path, Operation.MKDIRS);
        }
        
        runCommands(ctx, command);
        
        return result;
    }
    
    private List<String> createFiles(ExecutionContext ctx, int numberOf, List<String> folder) {
        final ExecutionCommand[] command = new ExecutionCommand[numberOf];
        final List<String> result = Lists.newArrayList();
        final RandomStringGenerator gen = builder.withinRange('a', 'z').build();
        
        for (int i = 0; i < numberOf; i++) {
            final String fileName = gen.generate(r.nextInt(5) + 5);
            final String folderName = folder.get(r.nextInt(folder.size()));
            final String path = folderName + "/" + fileName;
            
            result.add(path);
            command[i] = buildCommand(path, Operation.CREATE);
        }
        
        runCommands(ctx, command);
        
        return result;
    }
}
