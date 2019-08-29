package it.at.restfs.storage;

import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import it.at.restfs.storage.dto.Container;
import lombok.extern.slf4j.Slf4j;

/*
 
	  le diverse implementazioni della it.at.restfs.storage.Storage possono andare su servizi diversi ... 
	  ad ogni container è associata una di queste
	  lo scopo di questa classe è di risolvere l'implementazione da usare 
	  passando inoltre la config corretta da usare
	  
*/

@Slf4j
public class StorageResolver {
	
	private final Injector injector;
	private final ContainerRepository cRepo;

	@Inject
	public StorageResolver(Injector injector, ContainerRepository cRepo) {
		this.injector = injector;
		this.cRepo = cRepo;
	}
		
	public Storage get(UUID uuidC) {
		final Container container = cRepo.load(uuidC);		
		
		final Storage storage = injector.getInstance(
			Key.get(Storage.class, Names.named(container.getStorage().key))
		);
		
		LOGGER.debug("container {}/{} use {} as storage", container.getId(), container.getName(), storage.getClass().getSimpleName());
		
		return storage;
	}
}
