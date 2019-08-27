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
  
	  e passando inoltre la config da passare per poter istanziare l'impl corretta
	  
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
		
		//XXX get this from container instance and also it's configuration
		final String containerImpl = Storage.Implementation.FS.key;
		
		//XXX resolve StorageProvider or Factory for pass custom container config
		//XXX use http://google-guice.googlecode.com/svn/trunk/javadoc/com/google/inject/assistedinject/FactoryModuleBuilder.html
		final Storage storage = injector.getInstance(Key.get(Storage.class, Names.named(containerImpl)));
		
		LOGGER.debug("container {}/{} use {} as storage", container.getId(), container.getName(), storage.getClass().getSimpleName());
		
		return storage;
	}
}
