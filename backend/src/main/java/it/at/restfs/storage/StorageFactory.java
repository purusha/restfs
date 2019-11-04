package it.at.restfs.storage;

import java.util.UUID;

public interface StorageFactory<T extends Storage> {
	T create(UUID container);	
}
