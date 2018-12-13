package it.at.restfs;

import static java.nio.charset.Charset.defaultCharset;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
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

public class SmokeTests {
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        
        Lists.newArrayList(
            Stage0.class //, Stage1.class, Stage2.class
        ).forEach(s -> {
            try {
                
                System.out.println("process " + s.getSimpleName());
                s.newInstance().accept(UUID.randomUUID());
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });        

    }
        
}

class Stage0 extends Stage {

    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {
        createContainer(container);
        
        runCommands(
            container,                
            buildCommand("file", Operation.CREATE),
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS)
        );

        showDiff(
            printHierarchy(container)                
        );        
    }
    
}

class Stage1 extends Stage {

    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {
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

        showDiff(
            printHierarchy(container)
        );        
    }
    
}

class Stage2 extends Stage {

    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {
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
        
        showDiff(
            printHierarchy(container)
        );
    }
    
}

abstract class Stage implements Consumer<UUID> {
    
    final static List<String> LS = new ArrayList<String>();
    
    static {
        LS.add("/bin/ls");
        LS.add("-lR1");
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

    @SneakyThrows(value = {IOException.class, InterruptedException.class})
    Path printHierarchy(UUID container) {
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
            System.out.println("Success!");
            return Paths.get(root.getAbsolutePath(), container + ".tree");
        } else {
            System.out.println("Failure!");
            throw new RuntimeException("no file create for printHierarchy");
        }
    }

    @SuppressWarnings("unchecked")
    void runCommands(UUID container, Pair<String, Operation> ... cmds) {
        Arrays.stream(cmds).forEach(cmd -> {
            System.out.println("> " + cmd);
            
            try {
                
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
                throw new RuntimeException(e);
            }
            
        });
    }

    //XXX this implementation know's which is the real implementation
    void createContainer(UUID container) {
        getContainer(container).mkdir();
    }

    //XXX this implementation know's which is the real implementation
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
    
    /*

        1) create container
        2) lancia un data test (n call http)
        3) dalla root del container ... run $> ls -lR1 > file
        4) diff between expected and file to show diff
 
     */
        
}

