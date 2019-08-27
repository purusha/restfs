package it.at.restfs.storage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import it.at.restfs.storage.dto.FileStatus;
import it.at.restfs.storage.dto.FolderStatus;
import it.at.restfs.storage.dto.OpenFile;
import it.at.restfs.storage.dto.ResouceNotFoundException;
import lombok.SneakyThrows;

public class FileSystemStorage implements Storage {
	
    private RootFileSystem rfs;

	/*

        TODO
        
        - think about resolve method (try to write it better)
        - this class does NOT work under win32 SO

     */
	
	@Inject
	public FileSystemStorage(RootFileSystem rfs) {
		this.rfs = rfs;
		
	}

    @SneakyThrows(IOException.class)
    @Override
    public FolderStatus listStatus(UUID container, String path) {
        final Path realPath = resolve(container, path, false);
      
        final FolderStatus result = new FolderStatus();
        build(realPath, realPath.toFile(), result);   
        
        try(Stream<Path> stream = Files.list(realPath)) {
            final List<FileStatus> collect = stream
                .filter(Files::isRegularFile)
                .map(p -> {
                    try {
                        final FileStatus f = new FileStatus();         
                        build(realPath, p.toFile(), f);
                        
                        return f;
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());   
            
            result.setChildren(collect);
        }
        
        return result;
    }

    @SneakyThrows(IOException.class)
    @Override
    public FileStatus getStatus(UUID container, String path) {
        final Path realPath = resolve(container, path, false);
        
        final FileStatus result = new FileStatus();         
        build(realPath, realPath.toFile(), result);
        
        return result;
    }
    
    @SneakyThrows(IOException.class)
    @Override
    public OpenFile open(UUID container, String path) {        
        final Path realPath = resolve(container, path, false);
                
        final OpenFile result = new OpenFile();
        result.setPath(path);
        result.setContent(Files.readAllLines(realPath, StandardCharsets.UTF_8));

        return result;
    }

    @SneakyThrows(IOException.class)
    @Override
    public void make(UUID container, String path, AssetType type) {
        final Path realPath = resolve(container, path, true);
                
        switch(type) {
            case FOLDER: {
                Files.createDirectories(realPath);
            } break;
                
            case FILE: {
                final String parent = extractParentSubpath(realPath, container);
                resolve(container, parent, false); //XXX la directory parent deve esistere
                
                Files.createFile(realPath);
            } break;
        }
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
        
        try(Stream<Path> stream = Files.walk(realPath)) {
            stream
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);            
        }        
    }
    
    @Override
    public AssetType typeOf(UUID container, AbsolutePath path) {
        final Path realPath = resolve(container, path.getPath(), false);        
        final File realFile = realPath.toFile();        
        
        return realFile.isDirectory() ? AssetType.FOLDER : AssetType.FILE;
    }

    @SneakyThrows(IOException.class)
    @Override
    public String rename(UUID container, String path, String target) {
        final Path sourcePath = resolve(container, path, false);
        final String parent = extractParentSubpath(sourcePath, container);
        final Path targetPath = resolve(container, parent + "/" + target, true);
        
        Files.move(sourcePath, targetPath); //throw FileAlreadyExistsException when targetPath just exist !!?
        
        return extractSubpath(targetPath, container);
    }

    @SneakyThrows(IOException.class)
    @Override
    public String move(UUID container, String path, AbsolutePath target) {
        final Path sourcePath = resolve(container, path, false);
        final Path targetPath = resolve(container, target.getPath(), true);
        
        if (AssetType.FOLDER == typeOf(container, AbsolutePath.of(path))) {
            FileUtils.moveDirectoryToDirectory(sourcePath.toFile(), targetPath.toFile(), false);  
        } else {
            FileUtils.moveFileToDirectory(sourcePath.toFile(), targetPath.toFile(), false);  
        }
        
        return extractSubpath(targetPath, container);
    }

    @Override
    public boolean exist(UUID container) {
        try{
            resolve(container, "", false);
        } catch (Exception e) {
            return false;
        }

        return true;
    }    
    
    private String extractSubpath(Path path, UUID container) {
        return StringUtils.substringAfter(path.toFile().getAbsolutePath(), container.toString());
    }

    private String extractParentSubpath(Path path, UUID container) {
        return StringUtils.substringAfter(path.getParent().toFile().getAbsolutePath(), container.toString());
    }

    private Path resolve(UUID container, String path, boolean flag) {
        final Path realPath = rfs.containerPath(container, path);        
        final File realFile = realPath.toFile();        
        
        if (!realFile.exists() && !flag) {
            throw new ResouceNotFoundException("path " + path + " on " + container + " does not exist");
        }
        
        return realPath;
    }
    
    private void build(final Path path, final File file, final FileStatus result) throws IOException {
        result.setType(file.isFile() ? AssetType.FILE : AssetType.FOLDER);
        result.setName(file.getName());
        
        if (file.isFile()) {
            result.setLength(file.length());
        }
                        
        final BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        
        result.setCreated(
            new Date(attr.creationTime().to(TimeUnit.MILLISECONDS))
        );
        
        result.setModified(
            new Date(attr.lastModifiedTime().to(TimeUnit.MILLISECONDS))
        );
        
        result.setLastAccess(
            new Date(attr.lastAccessTime().to(TimeUnit.MILLISECONDS))
        );
    }
    
}
