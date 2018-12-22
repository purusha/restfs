package it.at.restfs;

import static java.nio.charset.Charset.defaultCharset;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import com.google.common.collect.Maps;
import it.at.restfs.http.HTTPListener;
import it.at.restfs.storage.FileSystemStorage;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

abstract class Stage implements Consumer<UUID> {
    
    final static List<String> LS = new ArrayList<String>();
    
    static {
        LS.add("/bin/ls");
        //LS.add("-lR1"); //on MAC
        LS.add("-R1"); //on LINUX
    }
    
    private final RestFs service;

    public Stage() {
        service = new Retrofit.Builder()
            .baseUrl(String.format(
                "http://%s:%d/%s/%s/", HTTPListener.HOST, HTTPListener.PORT, HTTPListener.APP_NAME, HTTPListener.VERSION                    
            ))
            .build()
            .create(RestFs.class);
        
    }

    private Path printHierarchy(UUID container) throws IOException, InterruptedException {
        final File root = getContainer(container);
                
        final ProcessBuilder pb = new ProcessBuilder(LS);        
        pb.directory(root);
        pb.redirectErrorStream(true);
        
        final Process process = pb.start();

        FileUtils.write(
            new File(root, container.toString() + ".tree"), 
            String.join(
                "\n", 
                IOUtils.readLines(
                    process.getInputStream(), defaultCharset()
                )
            ), 
            defaultCharset()
        );

        if (process.waitFor() == 0) {
            System.out.println("generate tree file: Success!");
            return Paths.get(root.getAbsolutePath(), container + ".tree");
        } else {
            System.out.println("generate tree file: Failure!");
            throw new RuntimeException("no file create for printHierarchy");
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void runCommands(ExecutionContext context, Triple<String, Operation, Map<String, String>> ... cmds) {
        Arrays
            .stream(cmds)
            .forEach(cmd -> remoteCall(context, cmd));
    }

    @SneakyThrows(value = {IllegalAccessException.class, InvocationTargetException.class, IOException.class})
    private void remoteCall(ExecutionContext context, Triple<String, Operation, Map<String, String>> cmd) {
        System.out.println("$> " + cmd);
        
        @SuppressWarnings("unchecked")
        final Call<ResponseBody> result = (Call<ResponseBody>)Arrays.stream(RestFs.class.getMethods())
            .filter(m -> StringUtils.equals(m.getName(), cmd.getMiddle().toString().toLowerCase()))
            .findFirst()
            .get()
            .invoke(
                service, cmd.getLeft(), "42", context.getContainer(), cmd.getRight()
            );
        
        final Response<ResponseBody> execute = result.execute();
        
        if (execute.isSuccessful()) {
            
            if (context.isPrintResponse()) {          
                System.out.println(execute.body().string() + "\n");
            }
            
        } else {
            
            if (context.isStopOnError()) {
                System.out.println();
                throw new NotSuccessfullResult(execute);                        
            } else {
                //System.out.println(execute);
                System.out.println(execute.errorBody().string() + "\n");                
            }
            
        }        
    }
    
    //XXX this code know's which is the real implementation ... is stupid
    public void createContainer(UUID container) {
        getContainer(container).mkdir();
    }

    //XXX this code know's which is the real implementation ... is stupid
    private File getContainer(UUID container) {
        final Path path = Paths.get(FileSystemStorage.ROOT + "/" + container);
        
        return path.toFile();
    }
    
    @SneakyThrows(value = {IOException.class, InterruptedException.class, URISyntaxException.class})
    public void showDiff(UUID container) {
        final URL resource = getClass().getClassLoader().getResource(this.getClass().getSimpleName() + ".tree");
        
        if (Objects.isNull(resource)) {
            System.out.println("can't run diff command because tree file does not exist");
            return;
        }
        
        final Path expected = Paths.get(resource.toURI());        
        final Path result = printHierarchy(container);
        //System.out.println("run diff command on " + expected + " and " + result);
        
        final ProcessBuilder diffCommand = diffCommand(expected, result);
        final Process process = diffCommand.start();
        
        final String diffOutput = String.join(
            "\n", 
            IOUtils.readLines(
                process.getInputStream(), defaultCharset()
            )
        ); 
        
        if (process.waitFor() == 0 && StringUtils.isBlank(diffOutput)) {
            System.out.println("diff: Success!\n");
        } else {
            System.out.println("diff: Failure!\n");
            System.out.println(diffOutput);
        }        
    }
    
    //triple => targetResouce, operation, queryParams
    protected Triple<String, Operation, Map<String, String>> buildCommand(String data, Operation op) {
        return buildCommand(data, op, Maps.newHashMap());
    }    

    //triple => targetResouce, operation, queryParams
    protected Triple<String, Operation, Map<String, String>> buildCommand(String data, Operation op, Map<String, String> query) {
        return Triple.of(data, op, query);
    }    
    
    protected Map<String, String> queryBuilder(String key, String value) {
        final Map<String, String> r = Maps.newHashMap();
        r.put(key, value);
      
        return r;
    }
    
    private ProcessBuilder diffCommand(Path p1, Path p2) {
        final List<String> diff = new ArrayList<String>();
        
//        if (StringUtils.equals("Mac OS X", System.getProperty("os.name"))) {
            diff.add("/usr/bin/diff");    
//        } else {
//            diff.add("/bin/diff");            
//        }
        
        diff.add(p1.toFile().getAbsolutePath());
        diff.add(p2.toFile().getAbsolutePath());
        
        final ProcessBuilder pb = new ProcessBuilder(diff);        
        pb.redirectErrorStream(true);
        
        return pb;
    }
    
    protected void expected(String expected, String result) {
        if(! StringUtils.equals(expected, result)){
            
            System.err.println("expected: " + expected);
            System.err.println("result: " + result);
            
            throw new RuntimeException("Not the same !!?");
        }
    }
        
    @Getter
    @Builder
    static class ExecutionContext {
        private final UUID container;
        private final boolean stopOnError; 
        private final boolean printResponse;
    }

    @Getter
    @RequiredArgsConstructor
    class NotSuccessfullResult extends RuntimeException {
        private static final long serialVersionUID = 1363056864961261367L;
        
        private final Response<ResponseBody> response;
    }
    
}

