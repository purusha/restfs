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
		return false;
	}

	@Override
	public FolderStatus listStatus(UUID container, String path) {
		return null;
	}

	@Override
	public FileStatus getStatus(UUID container, String path) {
		return null;
	}

	@Override
	public OpenFile open(UUID container, String path) {
		return null;
	}

	@Override
	public void make(UUID container, String path, AssetType folder) {
	}

	@Override
	public void append(UUID container, String path, String body) {
	}

	@Override
	public void delete(UUID container, String path) {
	}

	@Override
	public AssetType typeOf(UUID container, AbsolutePath path) {
		return null;
	}

	@Override
	public String rename(UUID container, String path, String target) {
		return null;
	}

	@Override
	public String move(UUID container, String path, AbsolutePath target) {
		return null;
	}

}
