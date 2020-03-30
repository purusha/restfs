package it.at.restfs.integration.noAuth;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.text.RandomStringGenerator;
import org.apache.commons.text.RandomStringGenerator.Builder;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import it.at.restfs.NoAuthBaseTest;
import it.at.restfs.Operation;
import okhttp3.ResponseBody;

public class RandomGenerator extends NoAuthBaseTest {
    
    private static final Random RANDOM = new Random();
    private static final String CONTENT = "All fall gala hall this\\is/a%test\t_~!@#$%^&*()dude";
    private static final String CR = "\n";
    private static final Builder BUILDER = new RandomStringGenerator.Builder();
    
    /*
        XXX into this class there are all equals methods ... for Performance stress test in parallel fashion !!? 
     */
    
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
            4) getstatus per ogni file
            5) append, append, append on some file
            6) download first file
            7) delete all files
            8) delete all folder
            
            wait
            
            9) call /stats
                 
     */
    
    private void pipe() throws Throwable {
        final ExecutionContext ctx = context();
        
        final List<String> folders = createFolders(ctx, 120);
        
        wait(1);        
        final List<String> files = createFiles(ctx, 250, folders);
        
        wait(1);        
        getStatus(ctx, folders, Operation.LISTSTATUS);
        
        wait(1);        
        getStatus(ctx, files, Operation.GETSTATUS);
        
        wait(1);
        final List<String> filesWithContent = append(ctx, files);
        
        wait(1);
        open(ctx, filesWithContent);
        
        wait(10);        
        final List<ResponseBody> r1 = runCommands(ctx, buildMgmtCommand(Operation.STATS));                
        
        int expected = (folders.size() * 2) + (files.size() * 2) + (filesWithContent.size() * 2); //don't count /stats call
        
        expected("{\"200\":" + expected + "}", Iterables.get(r1, 0).string());        
    }
    
    private void open(ExecutionContext ctx, List<String> files) {
        final ExecutionCommand[] command = new ExecutionCommand[files.size()];
        
        for (int i = 0; i < files.size(); i++) {
            command[i] = buildCommand(files.get(i), Operation.OPEN);
        }     
        
        runCommands(ctx, command);
        
        //dont' call expected(...) ?        
    }

    private List<String> append(ExecutionContext ctx, List<String> files) {
        final RandomStringGenerator gen = BUILDER.withinRange('a', 'z').build();
        final List<String> filesWithContent = Lists.newArrayList();        
        final List<ExecutionCommand> command = Lists.newArrayList();
        
        for (int i = 0; i < files.size(); i++) {
            if (RANDOM.nextInt(10) >= 6) {
                final String e = files.get(i);
                
                filesWithContent.add(e);
                
                if (RANDOM.nextBoolean()) {
                    command.add(
                        buildCommand(e, Operation.APPEND, CONTENT)
                    );                
                } else {
                    command.add(
                        buildCommand(e, Operation.APPEND, CONTENT + CR + CONTENT + CR + gen.generate(42))
                    );                                    
                }
            }
        }
        
        runCommands(
            ctx, 
            command.toArray(
                new ExecutionCommand[command.size()]
            )
        );
        
        return filesWithContent;
    }

    private void getStatus(ExecutionContext ctx, List<String> resource, Operation op) {
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
        final RandomStringGenerator gen = BUILDER.withinRange('a', 'z').build();
        
        for (int i = 0; i < numberOf; i++) {
            final String path = IntStream.range(1, RANDOM.nextInt(5) + 5)
                .mapToObj(x -> gen.generate(RANDOM.nextInt(5) + 5))                
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
