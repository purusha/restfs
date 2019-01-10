package it.at.restfs.storage;

import java.io.File;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Inject;
import lombok.SneakyThrows;

public class FileSystemContainerRepository implements ContainerRepository {
    
    private final ObjectMapper mapper;
    
    @Inject
    public FileSystemContainerRepository() {
        mapper = new ObjectMapper(new YAMLFactory());
    }

    @SneakyThrows
    @Override
    public Container load(UUID container) {
        return mapper.readValue(build(container), Container.class);
    }
    
    public static File build(UUID container) {
        return new File(FileSystemStorage.ROOT + "C-" + container + ".yaml");
    }

    @SneakyThrows
    @Override
    public void save(Container container) {
        mapper.writeValue(build(container.getId()), container);
    }

}
