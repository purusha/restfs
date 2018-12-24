package it.at.restfs;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.Lists;

public class SmokeTests {
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        
        final long startTime = System.currentTimeMillis();
        final ExecutorService service = Executors.newSingleThreadExecutor();        
//        final ExecutorService service = Executors.newFixedThreadPool(5);
        
        Lists.newArrayList(
            Stage0.class, Stage1.class, Stage2.class, Stage3.class, Stage4.class,
            Stage11.class, Stage12.class, Stage13.class, Stage14.class, 
            Stage21.class, Stage22.class
        ).forEach(s -> service.submit(() -> {

            try {
                
                final UUID container = UUID.randomUUID();
                System.out.println("\nprocess " + s.getSimpleName() + " on " + container);
                
                final Stage stage = s.newInstance();
                
                stage.createContainer(container);                
                stage.accept(container);
                stage.showDiff(container);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
                
        }));     
        
        service.shutdown();        
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        
        final long stopTime = System.currentTimeMillis();
        System.err.println("EXECUTION TIME: " + (stopTime - startTime) + " ms");        

    }        
}

class Stage0 extends Stage {

    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {
        runCommands(
            ExecutionContext.builder()
                .container(container)
                .stopOnError(true)
                .build(), 
            buildCommand("file", Operation.CREATE),
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS)
        );
    }    
}

class Stage1 extends Stage {

    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {
        runCommands(
            ExecutionContext.builder()
                .container(container)
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
    }    
}

class Stage11 extends Stage {
  
    /*
        non è possibile la rinomina di un file in uno che esiste già
     */

    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {        
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(container)
                    .stopOnError(true)
                    .build(), 
                buildCommand("file", Operation.CREATE),
                buildCommand("file2", Operation.CREATE),
                buildCommand("", Operation.LISTSTATUS),
                buildCommand("file", Operation.RENAME, queryBuilder("target", "file2"))
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=409, message=Conflict, url=http://localhost:8081/restfs/v1/file?op=RENAME&target=file2}", 
            r.getResponse().toString()
        );        
    }    
}

class Stage12 extends Stage {

    /*
        non è possibile la rinomina di un file che non esiste
    */
  
    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {        
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(container)
                    .stopOnError(true)
                    .build(), 
                buildCommand("file", Operation.CREATE),
                buildCommand("", Operation.LISTSTATUS),
                buildCommand("file2", Operation.RENAME, queryBuilder("target", "file3"))
            );   
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=404, message=Not Found, url=http://localhost:8081/restfs/v1/file2?op=RENAME&target=file3}", 
            r.getResponse().toString()
        );                
    }    
}

class Stage13 extends Stage {
    
    /*
        non è possibile la rinomina di una directory in una che esiste già
     */

    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {        
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(container)
                    .stopOnError(true)
                    .build(), 
                buildCommand("dir", Operation.MKDIRS),
                buildCommand("dir2", Operation.MKDIRS),
                buildCommand("", Operation.LISTSTATUS),
                buildCommand("dir", Operation.RENAME, queryBuilder("target", "dir2"))
            );
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=409, message=Conflict, url=http://localhost:8081/restfs/v1/dir?op=RENAME&target=dir2}", 
            r.getResponse().toString()
        );        
    }    
}

class Stage14 extends Stage {

    /*
        non è possibile la rinomina di una directory che non esiste
    */
  
    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {        
        NotSuccessfullResult r = null;
        
        try {
            
            runCommands(
                ExecutionContext.builder()
                    .container(container)
                    .stopOnError(true)
                    .build(), 
                buildCommand("dir", Operation.CREATE),
                buildCommand("", Operation.LISTSTATUS),
                buildCommand("dir2", Operation.RENAME, queryBuilder("target", "dir3"))
            );   
            
        } catch (NotSuccessfullResult e) {
            r = e;            
        }
        
        expected(
            "Response{protocol=http/1.1, code=404, message=Not Found, url=http://localhost:8081/restfs/v1/dir2?op=RENAME&target=dir3}",
            r.getResponse().toString()
        );                
    }    
}

class Stage2 extends Stage {

    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {
        runCommands(
            ExecutionContext.builder()
                .container(container)
                .stopOnError(true)
                .build(), 
            buildCommand("file", Operation.CREATE),
            buildCommand("dir", Operation.MKDIRS),
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS),
            buildCommand("file", Operation.RENAME, queryBuilder("target", "file2")),
            buildCommand("dir", Operation.RENAME, queryBuilder("target", "dir2")),
            buildCommand("file2", Operation.DELETE),
            buildCommand("dir2", Operation.DELETE)            
        );
    }    
}

class Stage21 extends Stage {

    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {
        runCommands(
            ExecutionContext.builder()
                .container(container)
                .stopOnError(true)
                .build(),  
            buildCommand("dir/dir2/dir3", Operation.MKDIRS),
            buildCommand("dir/dir2/dir3/test-no-extension", Operation.CREATE),
            buildCommand("dir/dir2/dir3/test.xml", Operation.CREATE),
            buildCommand("dir/dir2/dir3/test.json", Operation.CREATE),
            buildCommand("dir/dir2/dir4/dir5", Operation.MKDIRS),
            buildCommand("dir/dir2/dir4/dir5/file1.html", Operation.CREATE),
            buildCommand("dir/dir2/dir4/dir5/file2.html", Operation.CREATE),
            buildCommand("dir/dir2/dir4/dir5/dir6", Operation.MKDIRS),
            buildCommand("dir/dir2/dir4/dir5", Operation.DELETE)            
        );
    }    
}

class Stage22 extends Stage {

    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {
        runCommands(
            ExecutionContext.builder()
                .container(container)
                .stopOnError(true)
                .build(),
            buildCommand("dir/dir2/dir4/dir5/dir6", Operation.MKDIRS),
            buildCommand("dir/dir2/dir4", Operation.RENAME, queryBuilder("target", "dirX")),
            buildCommand("dir/dir2/dirX/file1.html", Operation.CREATE),
            buildCommand("dir/dir2/dirX/file1.html", Operation.RENAME, queryBuilder("target", "fileX.html"))
        );
    }    
}

class Stage3 extends Stage {
    
    /*
        operation on file/directory that does not exist
     */

    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {      
        runCommands(
            ExecutionContext.builder()
                .container(container)
                .stopOnError(false)
                .build(), 
            buildCommand("file", Operation.GETSTATUS),
            buildCommand("dir", Operation.LISTSTATUS),
            buildCommand("file", Operation.RENAME, queryBuilder("target", "file2")),
            buildCommand("dir", Operation.RENAME, queryBuilder("target", "dir2")),
            buildCommand("file", Operation.DELETE),
            buildCommand("dir", Operation.DELETE)
        ); 
        
        //XXX how to verify at least all http response status !!?
    }    
}

class Stage4 extends Stage {  
  
    @SuppressWarnings("unchecked")
    @Override
    public void accept(UUID container) {      
        runCommands(
            ExecutionContext.builder()
                .container(container)
                .stopOnError(true)
                .build(),
            buildCommand("file1", Operation.CREATE),
            buildCommand("file2", Operation.CREATE),
            buildCommand("dir/dir2", Operation.MKDIRS),               
            buildCommand("file1", Operation.MOVE, queryBuilder("target", "dir/dir2")),
            buildCommand("file2", Operation.MOVE, queryBuilder("target", "dir"))
        );       
    }    
}
