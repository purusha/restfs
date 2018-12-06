package it.at.restfs.storage;

import java.util.List;
import java.util.UUID;

public interface Storage {
    
    /*
        hide real implementation that use local fs as a default storage                
     */

    List<GetStatus> listStatus(UUID container, String path);

    GetStatus getStatus(UUID container, String path);

    void open(UUID container, String path);

    void make(UUID container, String path, AssetType folder);

    void append(UUID container, String path, String body);

}
