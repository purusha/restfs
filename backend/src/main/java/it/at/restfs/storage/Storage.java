package it.at.restfs.storage;

import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import it.at.restfs.storage.dto.FileStatus;
import it.at.restfs.storage.dto.FolderStatus;
import it.at.restfs.storage.dto.OpenFile;

public interface Storage {
	
	public enum Implementation {
		FS("fs", FileSystemStorage.Factory.class), 
		HDFS("hdfs", HdfsStorage.Factory.class); 
		
		public String key; 
		public Class<? extends StorageFactory<?>> factory;
		
		private Implementation(String k, Class<? extends StorageFactory<?>> f) {
			this.key = k;
			this.factory = f;
		}
	}	
	
    FolderStatus listStatus(AbsolutePath path);

    FileStatus getStatus(AbsolutePath path);

    OpenFile open(AbsolutePath path);

    void make(AbsolutePath path, AssetType folder);

    void append(AbsolutePath path, String body);
    
    void delete(AbsolutePath path);
    
    AssetType typeOf(AbsolutePath path);

    String rename(AbsolutePath path, AbsolutePath target);

    String move(AbsolutePath path, AbsolutePath target);

}
