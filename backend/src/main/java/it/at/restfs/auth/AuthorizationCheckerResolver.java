package it.at.restfs.auth;

import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.dto.Container;
import lombok.extern.slf4j.Slf4j;

/*
	
	le diverse implementazioni della it.at.restfs.auth.AuthorizationChecker possono andare su servizi diversi ... 
	ad ogni container è associata una di queste
	lo scopo di questa classe è di risolvere l'implementazione da usare 
	
*/

@Slf4j
public class AuthorizationCheckerResolver {

	private final Injector injector;
	private final ContainerRepository cRepo;

	@Inject
	public AuthorizationCheckerResolver(Injector injector, ContainerRepository cRepo) {
		this.injector = injector;
		this.cRepo = cRepo;
	}
	
	public AuthorizationChecker get(UUID uuidC) {
		final Container container = cRepo.load(uuidC);		
		
		final AuthorizationChecker checker = injector.getInstance(
			Key.get(AuthorizationChecker.class, Names.named(container.getAuthorization()))
		);
		
		LOGGER.debug("container {}/{} use {} as AuthorizationChecker", container.getId(), container.getName(), checker.getClass().getSimpleName());
				
		return checker;
	}
}
