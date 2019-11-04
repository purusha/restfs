package it.at.restfs.storage;

import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import it.at.restfs.storage.dto.FileStatus;
import it.at.restfs.storage.dto.FolderStatus;
import it.at.restfs.storage.dto.OpenFile;

public interface Storage {
	
	public enum Implementation {
		FS("fs", FileSystemStorage.class),
		HFS("hdfs", HdfsStorage.class);
		
		public String key; 
		public Class<? extends Storage> implClazz;
		
		private Implementation(String k, Class<? extends Storage> i) {
			this.key = k;
			this.implClazz = i;
		}
	}	
	
	/*
		
		TODO
		
		1) tutti i path dovrebero essere di tipo AbsolutePath e non String
		
	 */
    
    //boolean exist(UUID container);

    FolderStatus listStatus(String path);

    FileStatus getStatus(String path);

    OpenFile open(String path);

    void make(String path, AssetType folder);

    void append(String path, String body);
    
    void delete(String path);
    
    AssetType typeOf(AbsolutePath path);

    String rename(String path, String target);

    String move(String path, AbsolutePath target);

}
