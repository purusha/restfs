package it.at.restfs.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import it.at.restfs.event.Event;
import it.at.restfs.storage.dto.Container;
import lombok.SneakyThrows;

public class FileSystemContainerRepository implements ContainerRepository {
    
    private static String CONTAINER_PREFIX = "C-";
    private static String WEBHOOK_PREFIX = "WH-";
    private static String LAST_CALL_PREFIX = "LC-";
        
    /*
	
	    TODO
	    
	    - try to use the same strategy when persist LC && WH: in the first case append on a file, in the second append a file in folder 

	    - this class does NOT work under win32 SO
	
	 */
    
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
    
    @Override
    public boolean exist(UUID container) {
        try{
            return buildContainer(container).exists();
        } catch (Exception e) {
            return false;
        }
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
        
        //XXX optimize file content removing lines from the top
        //until lines count should be XXX (from container config)
                
    }

    @SneakyThrows
    @Override
    public List<UUID> findAll() {
        final Path source = Paths.get(FileSystemStorage.ROOT); //reuse me please !!?
        
        try(Stream<Path> stream = Files.list(source)) {
            return stream
                .filter(Files::isDirectory)
                .filter(folder -> ! StringUtils.startsWith(folder.toFile().getName(), WEBHOOK_PREFIX))
                .map(folder -> UUID.fromString(folder.toFile().getName()))
                .collect(Collectors.toList());                
        }
    }

//    @SuppressWarnings("unchecked")
//    @SneakyThrows
//    @Override
//    public List<Event> getWebhook(UUID container) {
//        //potrebbe ancora non esistere la folder relativa al container
//        if (! Files.exists(buildBaseWebHook(container))) {
//            return Lists.newArrayList();
//        }
//        
//        return (List<Event>) Files
//            .list(buildBaseWebHook(container))
//            .filter(Files::isRegularFile)
//            .map(p -> {
//                try {
//                    return mapper.<List<Event>>readValue(p.toFile(), new TypeReference<List<Event>>() { });
//                } catch (Exception e) {
//                    LOGGER.error("", e);
//                    return Lists.newArrayList();
//                }                
//            })
//            .filter(l -> ! l.isEmpty())
//            .flatMap(List::stream)
//            .collect(Collectors.toList());
//    }

    @SneakyThrows
    @Override    
    public List<Path> getWebhook(UUID container) {
        final Path rootWebHook = buildBaseWebHook(container);
        
        if (! Files.exists(rootWebHook)) {
            return Lists.newArrayList();
        }
        
        try(Stream<Path> stream = Files.list(rootWebHook)) {
            return stream
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());                
        }
    }

    @SneakyThrows
    @Override
    public List<Event> getCalls(UUID container) {
        final File lastCalls = buildLastCalls(container);
        
        if (! lastCalls.exists()) {
            return Lists.newArrayList();            
        }
        
        return mapper.<List<Event>>readValue(lastCalls, new TypeReference<List<Event>>() { });
    }    
    
//    @SneakyThrows
//    @Override
//    public void deleteWebhook(UUID container) {
//        Files.delete(buildBaseWebHook(container));
//    }

    private File buildContainer(UUID container) {
        return new File(FileSystemStorage.ROOT + CONTAINER_PREFIX + container + ".yaml");
    }
 
    @SneakyThrows
    private File buildWebHook(UUID container) {
        new File(FileSystemStorage.ROOT + WEBHOOK_PREFIX + container).mkdir(); //XXX create this when container is created !!?
        
        return buildBaseWebHook(container).resolve(String.valueOf(System.currentTimeMillis())).toFile();        
    }

    @SneakyThrows
    private Path buildBaseWebHook(UUID container) {
        return Paths.get(FileSystemStorage.ROOT + WEBHOOK_PREFIX + container);
    }
    
    @SneakyThrows
    private File buildLastCalls(UUID container) {
        return new File(FileSystemStorage.ROOT + LAST_CALL_PREFIX + container);
    }

}
