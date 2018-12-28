package it.at.restfs.storage;

import java.util.UUID;

public interface Storage {
    
    /*
        hide real implementation that use local fs as a default storage                
     */
    
    boolean exist(UUID container);

    FolderStatus listStatus(UUID container, String path);

    FileStatus getStatus(UUID container, String path);

    OpenFile open(UUID container, String path);

    void make(UUID container, String path, AssetType folder);

    void append(UUID container, String path, String body);
    
    void delete(UUID container, String path);
    
    AssetType typeOf(UUID container, AbsolutePath path);

    String rename(UUID container, String path, String target);

    String move(UUID container, String path, AbsolutePath target);

}
