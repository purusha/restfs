package it.at.restfs.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import it.at.restfs.auth.MasterPassword;
import it.at.restfs.event.EventView;
import it.at.restfs.storage.dto.Container;
import lombok.SneakyThrows;

public class FileSystemContainerRepository implements ContainerRepository {
    
    private final static String CONTAINER_PREFIX = "C-";	//file
    private final static String WEBHOOK_PREFIX = "WH-";		//folder
    private final static String LAST_CALL_PREFIX = "LC-";	//file
    private final static String STATISTICS_PREFIX = "S-";	//file
        
    /*
	
	    TODO
	    
	    - try to use the same strategy when persist LC && WH: in the first case append on a file, in the second append a file in folder 
	
	 */
    
    private final ObjectMapper mapper;
	private final RootFileSystem rfs;
    
    @Inject
    public FileSystemContainerRepository(RootFileSystem rfs) {
    	this.rfs = rfs;
		this.mapper = new ObjectMapper(new YAMLFactory());
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
        
        rfs.fileOf(WEBHOOK_PREFIX, container.getId()).mkdir();
    }

    @SneakyThrows
    @Override
    public void saveWebhook(UUID container, List<EventView> events) {
    	final File webHook = buildWebHook(container).resolve(String.valueOf(System.currentTimeMillis())).toFile();
    	
        mapper.writeValue(webHook, events);
    }
    
    @SneakyThrows
    @Override
    public void saveCalls(UUID container, List<EventView> events) {
        mapper.writeValue(buildLastCalls(container), events);
    }

    @SneakyThrows
    @Override
    public List<Container> findAll() {
        final Path source = Paths.get(rfs.getRoot());
        
        try(Stream<Path> stream = Files.list(source)) {
            return stream
                .filter(Files::isDirectory)
                .filter(folder -> ! StringUtils.startsWith(folder.toFile().getName(), WEBHOOK_PREFIX))
                .filter(folder -> ! StringUtils.startsWith(folder.toFile().getName(), MasterPassword.REPO_PREFIX))
                .map(folder -> UUID.fromString(folder.toFile().getName()))
                .map(this::load)
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
        try(Stream<Path> stream = Files.list(buildWebHook(container))) {
            return stream
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());                
        }
    }

    @SneakyThrows
    @Override
    public List<EventView> getCalls(UUID container) {
        final File lastCalls = buildLastCalls(container);
        
        if (! lastCalls.exists()) {
            return Lists.newArrayList();            
        }
        
        return mapper.<List<EventView>>readValue(lastCalls, new TypeReference<List<EventView>>() { });
    }    
    
//    @SneakyThrows
//    @Override
//    public void deleteWebhook(UUID container) {
//        Files.delete(buildBaseWebHook(container));
//    }  
    
    @SneakyThrows
	@Override
	public void saveStatistics(UUID container, Map<Integer, Long> statistics) {
		mapper.writeValue(buildStatistics(container), statistics);		
	}

    @SneakyThrows
	@Override
	public Map<Integer, Long> getStatistics(UUID container) {
        final File lastCalls = buildStatistics(container);
        
        if (! lastCalls.exists()) {
            return Maps.newHashMap();        
        }
        
        return mapper.<Map<Integer, Long>>readValue(lastCalls, new TypeReference<Map<Integer, Long>>() { });
	}

    private File buildContainer(UUID container) {
    	return rfs.fileOf(CONTAINER_PREFIX, container);
    }
 
    private Path buildWebHook(UUID container) {
    	return rfs.pathOf(WEBHOOK_PREFIX, container);
    }
    
    private File buildLastCalls(UUID container) {
    	return rfs.fileOf(LAST_CALL_PREFIX, container);
    }
    
    private File buildStatistics(UUID container) {
    	return rfs.fileOf(STATISTICS_PREFIX, container);
    }    
}
