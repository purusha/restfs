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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import it.at.restfs.auth.AuthorizationChecker;
import it.at.restfs.auth.AuthorizationChecker.Implementation;
import it.at.restfs.http.AdminHTTPListener;
import it.at.restfs.http.services.PathHelper;
import it.at.restfs.storage.RootFileSystem;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public abstract class Stage {
	
	/*
		TODO:
		
		1) remove System.out.println ... use logging please !!?
		
	 */
    
    private final RestFs service;
    protected final Admin admin;   
    
	//XXX please inject me !!?
	@Delegate
	private final IntegrationResolver ir = new IntegrationResolver();
    
    //XXX use Inject please !!?
    private final RootFileSystem rfs = new RootFileSystem();

    protected Stage() {        
        service = new Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(String.format(
                "http://%s:%d/%s/%s/", getPublicEndpoint().getKey(), getPublicEndpoint().getValue(), PathHelper.APP_NAME, PathHelper.VERSION                    
            ))
            .build()
            .create(RestFs.class);
        
        admin = new Retrofit.Builder()
            .baseUrl(String.format(
                "http://%s:%d/%s/%s/", getAdminEndpoint().getKey(), getAdminEndpoint().getValue(), PathHelper.APP_NAME, PathHelper.VERSION                    
            ))
            .build()
            .create(Admin.class);
    }
    
    private Path printHierarchy(UUID container) throws IOException, InterruptedException {
        final File root = getContainer(container);
                
        final ProcessBuilder pb = new ProcessBuilder(getFeatures().ls());        
        pb.directory(root);
        pb.redirectErrorStream(true);
        
        final Process process = pb.start();

        FileUtils.write(
            new File(root, container.toString() + ".tree"), 
            String.join("\n", getFeatures().catchOutputOf(process)), 
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
    
    /*
        XXX break test dependency from lib: return our ResponseBody 
     */
    @SuppressWarnings("deprecation")
    protected List<ResponseBody> runCommands(ExecutionContext context, ExecutionCommand ... cmds) {
        return Arrays
            .stream(cmds)
            .map(cmd -> remoteCall(context, cmd))
            .peek(body -> IOUtils.closeQuietly(body)) //close before return !!?
            .collect(Collectors.toList());
    }

    @SneakyThrows(value = {IllegalAccessException.class, InvocationTargetException.class, IOException.class})
    private ResponseBody remoteCall(ExecutionContext context, ExecutionCommand cmd) {
        System.out.println("$> " + cmd);
                        
        final Object[] callParams = cmd.callParams(context.getAuthHeader(), context.getContainer());
                
        @SuppressWarnings("unchecked")
        final Call<ResponseBody> result = (Call<ResponseBody>) Arrays.stream(RestFs.class.getMethods())
            .filter(m -> StringUtils.equals(m.getName(), cmd.getOperation().toString().toLowerCase()))
            .filter(m -> m.getParameterTypes().length == callParams.length)
            .findFirst()
            .get()
            .invoke(service, callParams);
        
        final Response<ResponseBody> execute = result.execute();
                
        if (execute.isSuccessful()) {
            
            if (context.isPrintResponse()) {          
                System.out.println(execute.body().string() + "\n");
            }
            
            return execute.body();           
        } else {
            
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
    
    @SneakyThrows
    protected void createContainer(ExecutionContext ctx) {
    	
    	final Map<String, String> fields = new HashMap<String, String>();
    	fields.put("id", ctx.getContainer().toString());
    	fields.put("statsEnabled", Boolean.TRUE.toString());
    	fields.put("webHookEnabled", Boolean.TRUE.toString());
    	fields.put("authorization", ctx.getType().name());
    	
    	if (ctx.getType() == Implementation.MASTER_PWD) {
    		fields.put("masterPwd", ctx.getAuthHeader());
    	}
    	
        final Call<ResponseBody> create = admin.create(AdminHTTPListener.CONTAINERS, fields);
        
        final Response<ResponseBody> execute = create.execute();
        
        if (execute.isSuccessful()) {
            //close the stream before return !!?
            execute.body().close();   
        } else {
            //close the stream before return !!?
            execute.errorBody().close();
            
            System.err.println(execute.errorBody().string());
        }             
    }

    //XXX this code know's which is the real implementation ... is stupid
    private File getContainer(UUID container) {
        return rfs.containerPath(container, "").toFile();
    }
    
    @SneakyThrows(value = {IOException.class, InterruptedException.class, URISyntaxException.class})
    protected void showDiff(UUID container) { //XXX get an ExecutionContext as parameter
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
    
    protected ExecutionCommand buildMgmtCommand(Operation o) {    	
    	if (! Operation.isManagement(o)) {
    		throw new RuntimeException("Use this api only for management operations");
    	}
    	
    	switch(o) {
    		case STATS: 
    			return new StatsExecutionCommand();
    			
    		case TOKEN: 
    			return new TokenExecutionCommand();
    			
    		default: 
    			return null; //XXX eheheh ???
    	}
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
        
        diff.add("/usr/bin/diff"); //XXX how to execute this on Win32 machine ?   
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
		private final String authHeader;
		private final AuthorizationChecker.Implementation type;    
		
    }

    public static interface ExecutionCommand {
        
        Operation getOperation();
        
        Object[] callParams(String authorization, UUID container);
        
    }
    
    private static class SimpleExecutionCommand implements ExecutionCommand {
        
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
        
    private static class StatsExecutionCommand implements ExecutionCommand {

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
    
    private static class TokenExecutionCommand implements ExecutionCommand {

        @Override
        public Operation getOperation() {
            return Operation.TOKEN;
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
            return "call TOKEN";
        }        
    }    

    @Getter
    @RequiredArgsConstructor
    public class NotSuccessfullResult extends RuntimeException {
        private static final long serialVersionUID = 1363056864961261367L;
        
        private final Response<ResponseBody> response;
    }
        
}

