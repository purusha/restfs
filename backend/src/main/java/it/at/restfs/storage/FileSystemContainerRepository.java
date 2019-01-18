package it.at.restfs.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Inject;
import it.at.restfs.event.Event;
import lombok.SneakyThrows;

public class FileSystemContainerRepository implements ContainerRepository {
    
    private static String CONTAINER_PREFIX = "C-";
    private static String WEBHOOK_PREFIX = "WH-";
    private static String LAST_CALL_PREFIX = "LC-";
    
    private final ObjectMapper mapper;
    
    @Inject
    public FileSystemContainerRepository() {
        mapper = new ObjectMapper(new YAMLFactory());
    }

    @SneakyThrows
    @Override
    public Container load(UUID container) {
        return mapper.readValue(buildContainer(container), Container.class);
    }
    
    @SneakyThrows
    @Override
    public void save(Container container) {
        mapper.writeValue(buildContainer(container.getId()), container);
    }

    @SneakyThrows
    @Override
    public void saveWebhook(UUID container, List<Event> events) {
        mapper.writeValue(buildWebHook(container), events);
    }
    
    @SneakyThrows
    @Override
    public void saveCalls(UUID container, List<Event> events) {
        
        //append
        mapper.writeValue(buildLastCalls(container), events);
        
        //optimize file content removing lines from the top
        //until lines count should be XXX (from container config)
                
    }

    //XXX exposed only for test 
    public static File buildContainer(UUID container) {
        return new File(FileSystemStorage.ROOT + CONTAINER_PREFIX + container + ".yaml");
    }
 
    @SneakyThrows
    private static File buildWebHook(UUID container) {
        new File(FileSystemStorage.ROOT + WEBHOOK_PREFIX + container).mkdir(); //XXX create this when container is created !!?
        
        return new File(FileSystemStorage.ROOT + WEBHOOK_PREFIX + container + "/" + System.currentTimeMillis());
    }

    @SneakyThrows
    private static PrintWriter buildLastCalls(UUID container) {
        final File file = new File(FileSystemStorage.ROOT + LAST_CALL_PREFIX + container);
        
        return new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
    }
    
}
