package it.at.restfs;

import static java.nio.charset.Charset.defaultCharset;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.at.restfs.http.HTTPListener;
import it.at.restfs.storage.FileSystemContainerRepository;
import it.at.restfs.storage.FileSystemStorage;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public abstract class Stage {
  
    private static final org.apache.commons.text.RandomStringGenerator TEXT_BUILDER = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
    
    public static final String _42 = "42"; //XXX 42 is not a really auth value header !!?
    
    private final OSFeatures features;
    private final RestFs service;

    protected Stage() {
        service = new Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(String.format(
                "http://%s:%d/%s/%s/", HTTPListener.HOST, HTTPListener.PORT, HTTPListener.APP_NAME, HTTPListener.VERSION                    
            ))
            .build()
            .create(RestFs.class);
                
        features = OSFeatures.build();
    }
    
    private Path printHierarchy(UUID container) throws IOException, InterruptedException {
        final File root = getContainer(container);
                
        final ProcessBuilder pb = new ProcessBuilder(features.ls());        
        pb.directory(root);
        pb.redirectErrorStream(true);
        
        final Process process = pb.start();

        FileUtils.write(
            new File(root, container.toString() + ".tree"), 
            String.join("\n", features.catchOutputOf(process)), 
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
    
    protected List<ResponseBody> runCommands(ExecutionContext context, ExecutionCommand ... cmds) {
        return Arrays
            .stream(cmds)
            .map(cmd -> remoteCall(context, cmd))
            .collect(Collectors.toList());
    }

    @SneakyThrows(value = {IllegalAccessException.class, InvocationTargetException.class, IOException.class})
    private ResponseBody remoteCall(ExecutionContext context, ExecutionCommand cmd) {
        System.out.println("$> " + cmd);
                        
        final Object[] callParams = cmd.callParams(_42, context.getContainer());
//        System.out.println(Arrays.toString(callParams));
                
        @SuppressWarnings("unchecked")
        final Call<ResponseBody> result = (Call<ResponseBody>)Arrays.stream(RestFs.class.getMethods())
            .filter(m -> StringUtils.equals(m.getName(), cmd.getOperation().toString().toLowerCase()))
            .filter(method -> method.getParameterTypes().length == callParams.length)
            .findFirst()
            .get()
            .invoke(service, callParams);
        
        final Response<ResponseBody> execute = result.execute();
                
        if (execute.isSuccessful()) {
            //close the stream before return !!?
            execute.body().close();            
            
            if (context.isPrintResponse()) {          
                System.out.println(execute.body().string() + "\n");
            }
            
            return execute.body();           
        } else {
            //close the stream before return !!?
            execute.errorBody().close();
            
            if (context.isPrintResponse()) {          
                System.err.println(execute.errorBody().string() + "\n");
            }
                        
            if (context.isStopOnError()) {
                System.err.println();
                throw new NotSuccessfullResult(execute);                        
            }
            
            return execute.errorBody();            
        }        
    }
    
    //XXX this code know's which is the real implementation ... is stupid
    @SneakyThrows
    protected void createContainer(UUID container) {
        getContainer(container).mkdir();
        
        final URL cTemplate = getClass().getClassLoader().getResource("c-template.yaml");
        final String template = IOUtils.toString(cTemplate, StandardCharsets.UTF_8);
        
        IOUtils.write(
            StringUtils.replaceEach(
                template, 
                new String[]{"${name}", "${id}"}, 
                new String[]{TEXT_BUILDER.generate(12), container.toString()}
            ),
            new FileOutputStream(FileSystemContainerRepository.build(container)), 
            StandardCharsets.UTF_8
        );
    }

    //XXX this code know's which is the real implementation ... is stupid
    private File getContainer(UUID container) {
        final Path path = Paths.get(FileSystemStorage.ROOT + "/" + container);
        
        return path.toFile();
    }
    
    @SneakyThrows(value = {IOException.class, InterruptedException.class, URISyntaxException.class})
    protected void showDiff(UUID container) {
        final URL resource = getClass().getClassLoader().getResource(this.getClass().getSimpleName() + ".tree");
        
        if (Objects.isNull(resource)) {
            System.out.println("diff: can't run because tree file does not exist");
            return;
        }
        
        final Path expected = Paths.get(resource.toURI());        
        final Path result = printHierarchy(container);        
        final Process process = diffCommand(expected, result).start();
        
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
    
    protected ExecutionCommand buildStatsCommand() {
        return new StatsExecutionCommand();
    }
    
    protected ExecutionCommand buildCommand(String data, Operation op) {
        return new SimpleExecutionCommand(data, op, null);
    }    

    protected ExecutionCommand buildCommand(String data, Operation op, Map<String, String> query) {       
        return new SimpleExecutionCommand(data, op, query);
    }    

    protected ExecutionCommand buildCommand(String data, Operation op, String body) {                                
        return new SimpleExecutionCommand(data, op, body);
    }    
        
    protected Map<String, String> queryBuilder(String key, String value) {
        final Map<String, String> r = Maps.newHashMap();
        r.put(key, value);
      
        return r;
    }
    
    private ProcessBuilder diffCommand(Path p1, Path p2) {
        final List<String> diff = new ArrayList<String>();
        
        diff.add("/usr/bin/diff");    
        diff.add(p1.toFile().getAbsolutePath());
        diff.add(p2.toFile().getAbsolutePath());
        
        final ProcessBuilder pb = new ProcessBuilder(diff);        
        pb.redirectErrorStream(true);
        
        return pb;
    }
            
    @Getter
    @Builder
    public static class ExecutionContext {
        private final UUID container;
        private final boolean stopOnError; 
        private final boolean printResponse;                
    }

    public static interface ExecutionCommand {
        
        Operation getOperation();
        
        Object[] callParams(String authorization, UUID container);
        
    }
    
    public static class SimpleExecutionCommand implements ExecutionCommand {
        
        @Getter
        private final Operation operation;
        
        private final String resouce;          
        private final Map<String, String> query;
        private final String body;
        
        @SuppressWarnings("unchecked")
        private SimpleExecutionCommand(String targetResouce, Operation operation, Object o) {
            this.resouce = targetResouce;
            this.operation = operation;
            
            if (o instanceof Map) {
                this.query = (Map<String, String>) o;    
            } else {
                this.query = null;
            }
            
            if (o instanceof String) {
                this.body = (String) o;                
            } else {
                this.body = null;    
            }
        }
        
        //XXX this implementation is coupled to RestFs methods signature
        @Override
        public Object[] callParams(String authorization, UUID container) {
            final List<Object> result = Lists.newArrayList();
            
            result.add(resouce);
            result.add(authorization);
            result.add(container);
            
            if (Objects.nonNull(query)) {
                result.add(query);
            }
            
            if (Objects.nonNull(body)) {
                result.add(body);
            }
            
            return result.toArray(new Object[result.size()]);
        }
        
        @Override
        public String toString() {
            return "call " + operation + " on " + resouce + " with query=" + query + " and body=" + body;
        }
    }
        
    public static class StatsExecutionCommand implements ExecutionCommand {

        @Override
        public Operation getOperation() {
            return Operation.STATS;
        }

        //XXX this implementation is coupled to RestFs methods signature
        @Override
        public Object[] callParams(String authorization, UUID container) {
            final List<Object> result = Lists.newArrayList();

            result.add(authorization);
            result.add(container);
            
            return result.toArray(new Object[result.size()]);
        }

        @Override
        public String toString() {
            return "call STATS";
        }        
    }

    @Getter
    @RequiredArgsConstructor
    public class NotSuccessfullResult extends RuntimeException {
        private static final long serialVersionUID = 1363056864961261367L;
        
        private final Response<ResponseBody> response;
    }
    
}

