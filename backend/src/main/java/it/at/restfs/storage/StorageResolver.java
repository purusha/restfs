package it.at.restfs.storage;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import it.at.restfs.storage.Storage.Implementation;
import it.at.restfs.storage.dto.Container;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 
	  le diverse implementazioni della it.at.restfs.storage.Storage possono andare su servizi diversi ... 
	  ad ogni container è associata una di queste
	  lo scopo di questa classe è di risolvere l'implementazione da usare 
	  
*/

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StorageResolver {
	
	private final ContainerRepository cRepo;
	private final Map<Implementation, StorageFactory<?>> binder;
	
	public Storage get(UUID uuidC) {
		final Container container = cRepo.load(uuidC);
		
		final Implementation impl = Arrays.stream(Implementation.values())
			.filter(i -> StringUtils.equals(i.key, container.getStorage()))
			.findFirst()
			.get();
		
		LOGGER.debug("container {}/{} use {} as Storage", container.getId(), container.getName(), impl.implClazz.getSimpleName());
		
		return binder.get(impl).create(uuidC);
	}
	
}
