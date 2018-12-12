package it.at.restfs.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import it.at.restfs.http.HTTPListener;
import lombok.SneakyThrows;

public class FileSystemStorage implements Storage {
    
    private static final String ROOT = "/tmp/" + HTTPListener.APP_NAME + "/";

    @SneakyThrows(IOException.class)
    @Override
    public List<GetStatus> listStatus(UUID container, String path) {
        final Path realPath = resolve(container, path, false);
        
        return Files
            .list(realPath)
            .filter(Files::isRegularFile)
            .map(p -> {
                try {
                    return build(p, p.toFile());
                } catch (IOException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @SneakyThrows(IOException.class)
    @Override
    public GetStatus getStatus(UUID container, String path) {
        final Path realPath = resolve(container, path, false);
        
        return build(realPath, realPath.toFile());
    }
    
    @SneakyThrows(IOException.class)
    @Override
    public OpenFile open(UUID container, String path) {        
        final Path realPath = resolve(container, path, false);
        
        if(AssetType.FOLDER == typeOf(container, path).get()) {
            throw new RuntimeException("can't download directory " + path + " on " + container);
        }
        
        final OpenFile result = new OpenFile();
        result.setPath(path);
        result.setContent(new String(Files.readAllBytes(realPath)));

        return result;
    }

    @SneakyThrows(IOException.class)
    @Override
    public void make(UUID container, String path, AssetType type) {
        final Path realPath = resolve(container, path, true);
        
        switch(type) {
            case FOLDER:
                Files.createDirectories(realPath);
                break;
                
            case FILE:
                Files.createFile(realPath);
                break;
        }
    }

    private Path resolve(UUID container, String path, boolean flag) {
        final Path realPath = Paths.get(ROOT + "/" + container + path);        
        final File realFile = realPath.toFile();        
        
        if (!realFile.exists() && !flag) {
            throw new RuntimeException("path " + path + " on " + container + " does not exist");
        }

        return realPath;
    }
    
    private GetStatus build(Path path, File file) throws IOException {
        final GetStatus result = new GetStatus();
        
        result.setName(file.getName());
        result.setHidden(file.isHidden());        
        result.setLength(file.length());        
        
        final Permission permission = new Permission();
        permission.setR(file.canRead() ? 'R' : '-');
        permission.setW(file.canWrite() ? 'W' : '-');
        permission.setE(file.canExecute() ? 'X' : '-');
        
        result.setPermission(permission);
        
        final BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        
        result.setCreated(
            new Date(attr.creationTime().to(TimeUnit.MILLISECONDS)));
        
        result.setModified(
            new Date(attr.lastModifiedTime().to(TimeUnit.MILLISECONDS)));
        
        result.setLastAccess(
            new Date(attr.lastAccessTime().to(TimeUnit.MILLISECONDS)));
                
        return result;
    }

    @SneakyThrows(IOException.class)
    @Override
    public void append(UUID container, String path, String body) {
        final Path realPath = resolve(container, path, false);

        Files.write(realPath, body.getBytes(), StandardOpenOption.APPEND);
    }

    @SneakyThrows(IOException.class)
    @Override
    public void delete(UUID container, String path) {
        final Path realPath = resolve(container, path, false);
        
        Files.delete(realPath);
    }
    
    @Override
    public Optional<AssetType> typeOf(UUID container, String path) {
        final Path realPath = Paths.get(ROOT + "/" + container + path);        
        final File realFile = realPath.toFile();        
        
        if (! realFile.exists()) {
            return Optional.empty();
        }
        
        return Optional.of(realFile.isDirectory() ? AssetType.FOLDER : AssetType.FILE);
    }

    @SneakyThrows(IOException.class)
    @Override
    public String rename(UUID container, String path, String target) {
        final Path sourcePath = resolve(container, path, false);
        
        final Path targetPath = sourcePath.resolveSibling(path);
        //final Path targetPath = resolve(container, target, true);
        
        Files.move(sourcePath, targetPath);
        
        return StringUtils.substringAfter(targetPath.toFile().getAbsolutePath(), container.toString());
    }    
    
}
