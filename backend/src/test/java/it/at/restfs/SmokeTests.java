package it.at.restfs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.Lists;
import it.at.restfs.http.HTTPListener;
import it.at.restfs.storage.FileSystemStorage;
import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class SmokeTests {
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        
        Lists.newArrayList(
            Stage0.class //, Stage1.class, Stage2.class
        )
            .forEach(s -> {
                
                try {
                    System.out.println("process " + s.getSimpleName());
                    s.newInstance().doOperations();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            });        

    }
        
}

class Stage0 extends Stage {

    @SuppressWarnings("unchecked")
    @Override
    void doOperations() {

        final UUID container = UUID.randomUUID();
        
        createContainer(container);
        
        runCommands(
            container,                
            buildCommand("file", Operation.CREATE),
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS)
        );

        final Path h = printHierarchy(container);
        
        showDiff(h);
        
    }
    
}

class Stage1 extends Stage {

    @SuppressWarnings("unchecked")
    @Override
    void doOperations() {

        final UUID container = UUID.randomUUID();
        
        createContainer(container);
        
        runCommands(
            container, 
            buildCommand("file", Operation.CREATE),
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS),
            buildCommand("file&target=file2", Operation.RENAME),
            buildCommand("dir&target=dir2", Operation.RENAME),
            buildCommand("file2", Operation.GETSTATUS),
            buildCommand("dir2", Operation.LISTSTATUS)            
        );

        final Path h = printHierarchy(container);
        
        showDiff(h);
        
    }
    
}

class Stage2 extends Stage {

    @SuppressWarnings("unchecked")
    @Override
    void doOperations() {

        final UUID container = UUID.randomUUID();
        
        createContainer(container);
        
        runCommands(
            container,                
            buildCommand("file", Operation.CREATE),
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS),
            buildCommand("file&target=file2", Operation.RENAME),
            buildCommand("dir&target=dir2", Operation.RENAME),
            buildCommand("file2", Operation.DELETE),
            buildCommand("dir2", Operation.DELETE)            
        );
        
        final Path h = printHierarchy(container);
        
        showDiff(h);
        
    }
    
}

abstract class Stage {
    
    private final RestFs service;

    public Stage() {
        service = new Retrofit.Builder()
            .baseUrl(String.format(
                "http://%s:%d/%s/%s/", HTTPListener.HOST, HTTPListener.PORT, HTTPListener.APP_NAME, HTTPListener.VERSION                    
            ))
            .build()
            .create(RestFs.class);
        
    }

    @SneakyThrows(value = {IOException.class, InterruptedException.class})
    Path printHierarchy(UUID container) {
        final File root = getContainer(container);
        
        final List<String> commands = new ArrayList<String>();
        commands.add("/bin/ls");
        commands.add("-lR1");
        
        final ProcessBuilder pb = new ProcessBuilder(commands);        
        pb.directory(root);
        pb.redirectErrorStream(true);
        
        final Process process = pb.start();

        FileUtils.write(
            new File(root, container.toString() + ".tree"), 
            String.join(
                "\n", 
                IOUtils.readLines(
                    process.getInputStream(), Charset.defaultCharset()
                )
            ), 
            Charset.defaultCharset()
        );

        //Check result
        if (process.waitFor() == 0) {
            System.out.println("Success!");
            return Paths.get(root.getAbsolutePath(), container + ".tree");
        } else {
            System.out.println("Failure!");
            throw new RuntimeException("no file create for printHierarchy");
        }
    }

    void runCommands(UUID container, Pair<String, Operation> ... cmds) {
        Arrays.stream(cmds).forEach(cmd -> {
            System.out.println("> " + cmd);
            
            try {
                
                @SuppressWarnings("unchecked")
                final Call<Void> result = (Call<Void>)Arrays.stream(RestFs.class.getMethods())
                    .filter(m -> StringUtils.equals(m.getName(), cmd.getRight().toString().toLowerCase()))
                    .findFirst()
                    .get()
                    .invoke(
                        service, cmd.getLeft(), "42", container, "application/json"
                    );
                
                final Response<Void> execute = result.execute();
                System.out.println(execute);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //XXX this implementation know which is the real implementation
    void createContainer(UUID container) {
        getContainer(container).mkdir();
    }

    private File getContainer(UUID container) {
        final Path path = Paths.get(FileSystemStorage.ROOT + "/" + container);
        
        return path.toFile();
    }
    
    void showDiff(Path result) {
        final String expectedResult = this.getClass().getSimpleName() + ".tree";
        final URL resource = getClass().getClassLoader().getResource(expectedResult);
        
        System.out.println(resource);
    }        
    
    Pair<String, Operation> buildCommand(String data, Operation op) {
        return Pair.of(data, op);
    }    
    
    abstract void doOperations();
    
    enum Operation {
        GETSTATUS, LISTSTATUS, OPEN,                        //GET
        MOVE, RENAME,                                       //PUT
        MKDIRS, CREATE, APPEND,                             //POST
        DELETE,                                             //DELETE
    }    
    
}

interface RestFs {
    
    @POST("{path}?op=CREATE")
    Call<Void> create(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @Header("Accept") String accept
    );

    @POST("{path}?op=MKDIRS")
    Call<Void> mkdirs(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @Header("Accept") String accept
    );

    @GET("{path}?op=GETSTATUS")
    Call<Void> getstatus(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @Header("Accept") String accept
    );

    @GET("{path}?op=LISTSTATUS")
    Call<Void> liststatus(
        @retrofit2.http.Path("path") String path,
        @Header(HTTPListener.AUTHORIZATION) String authorization,
        @Header(HTTPListener.X_CONTAINER) UUID container,
        @Header("Accept") String accept
    );
    
}
