package it.at.restfs.storage;

import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import it.at.restfs.storage.dto.FileStatus;
import it.at.restfs.storage.dto.FolderStatus;
import it.at.restfs.storage.dto.OpenFile;

public class HdfsStorage implements Storage {
	
	public interface Factory extends StorageFactory<HdfsStorage> {
		HdfsStorage create(UUID container);		
	}	
	
	//private final UUID container;
	
	@Inject
	public HdfsStorage(@Assisted UUID container) {
		//this.container = container;
	}	

	/*
	@Override
	public boolean exist(UUID container) {
		return false;
	}
	*/

	@Override
	public FolderStatus listStatus(String path) {
		return null;
	}

	@Override
	public FileStatus getStatus(String path) {
		return null;
	}

	@Override
	public OpenFile open(String path) {
		return null;
	}

	@Override
	public void make(String path, AssetType folder) {
	}

	@Override
	public void append(String path, String body) {
	}

	@Override
	public void delete(String path) {
	}

	@Override
	public AssetType typeOf(AbsolutePath path) {
		return null;
	}

	@Override
	public String rename(String path, String target) {
		return null;
	}

	@Override
	public String move(String path, AbsolutePath target) {
		return null;
	}

}
