package it.at.restfs.storage;

import java.util.UUID;

import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import it.at.restfs.storage.dto.FileStatus;
import it.at.restfs.storage.dto.FolderStatus;
import it.at.restfs.storage.dto.OpenFile;

public interface Storage {
	
	public enum Implementation {
		FS("fs", FileSystemStorage.class),
		HFS("hdfs", HsfsStorage.class);
		
		public String key; 
		public Class<? extends Storage> implClazz;
		
		private Implementation(String k, Class<? extends Storage> i) {
			this.key = k;
			this.implClazz = i;
		}
	}
	
    
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
