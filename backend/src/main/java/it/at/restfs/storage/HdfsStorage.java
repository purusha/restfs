package it.at.restfs.storage;

import java.util.UUID;

import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import it.at.restfs.storage.dto.FileStatus;
import it.at.restfs.storage.dto.FolderStatus;
import it.at.restfs.storage.dto.OpenFile;

public class HdfsStorage implements Storage {

	@Override
	public boolean exist(UUID container) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FolderStatus listStatus(UUID container, String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileStatus getStatus(UUID container, String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OpenFile open(UUID container, String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void make(UUID container, String path, AssetType folder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void append(UUID container, String path, String body) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(UUID container, String path) {
		// TODO Auto-generated method stub

	}

	@Override
	public AssetType typeOf(UUID container, AbsolutePath path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String rename(UUID container, String path, String target) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String move(UUID container, String path, AbsolutePath target) {
		// TODO Auto-generated method stub
		return null;
	}

}
