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

	  lo scopo di questa classe è di risolvere, per un determinato container l'implementazione da usare 
  
*/

@Slf4j
public class StorageResolver {
	
	private final Injector injector;
	private final ContainerRepository containerRepository;

	@Inject
	public StorageResolver(Injector injector, ContainerRepository containerRepository) {
		this.injector = injector;
		this.containerRepository = containerRepository;
	}
		
	public Storage get(UUID uuidC) {
		final Container container = containerRepository.load(uuidC);	
		final Storage storage = injector.getInstance(Key.get(Storage.class, Names.named(Storage.Implementation.FS.key)));
		
		LOGGER.debug("container {}/{} use {} as storage", container.getId(), container.getName(), storage.getClass().getSimpleName());
		
		return storage;
	}
}
