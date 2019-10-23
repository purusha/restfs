package it.at.restfs.auth;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.inject.Inject;

import it.at.restfs.storage.RootFileSystem;
import it.at.restfs.storage.dto.Container;
import lombok.SneakyThrows;

public class MasterPassword implements AuthorizationChecker, AuthorizationMaker {
	
	public final static String REPO_PREFIX = "AUTH-REPO-";	//folder

	private final RootFileSystem rfs;
	
	@Inject
    public MasterPassword(RootFileSystem rfs) {
    	this.rfs = rfs;
    }
	
	@SneakyThrows
	@Override
	public boolean isTokenValid(UUID container, Optional<String> authorization) {		
		if (!authorization.isPresent()) {
			return false;
		}
		
		try(Stream<Path> stream = Files.list(buildRepository(container))) {
            return stream
                .filter(Files::isRegularFile)
                .filter(p -> p.endsWith(authorization.get()))
                .count() == 1;                
        }		
	}

	@SneakyThrows
	@Override
	public String creteToken(Container container) {
		final String token = UUID.randomUUID().toString();
		
		final Path repository = buildRepository(container.getId());
				
		repository.toFile().mkdir(); //create folder
		
		repository.resolve(token).toFile().createNewFile(); //create file
		
		return token;
	}
	
	private Path buildRepository(UUID container) { 
    	return rfs.pathOf("AUTH-REPO-", container);
    }	
	
}
