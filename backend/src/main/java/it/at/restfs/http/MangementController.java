package it.at.restfs.http;

import static akka.http.javadsl.server.Directives.complete;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import it.at.restfs.auth.AuthorizationChecker;
import it.at.restfs.auth.AuthorizationChecker.Implementation;
import it.at.restfs.event.Event;
import it.at.restfs.http.ControllerRunner.ContainerAuth;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.http.services.Complete;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.dto.Container;

//@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MangementController implements Controller {
	
	private final ContainerRepository cRepo;
	//private final AuthorizationCheckerResolver authResolver;
	
	@Inject
    public MangementController(ContainerRepository cRepo) {
		this.cRepo = cRepo;
	}	
	
    //XXX should be executed in a Future ?	
    public Route stats(Request t) {        
        return complete(
            StatusCodes.OK, 
            cRepo.getStatistics(t.getContainer()),
            Jackson.<Map<Integer, Long>>marshaller()
        );
    }

	//XXX should be executed in a Future ?    
    public Route last(Request t) {        
        return complete(
            StatusCodes.OK, 
            cRepo.getCalls(t.getContainer()), 
            Jackson.<List<Event>>marshaller()
        );
    }	

    //XXX should be executed in a Future ?
	public Route token(ContainerAuth ctx) { 		
		
		final Container c = cRepo.load(ctx.getContainer());		
		final Implementation authType = AuthorizationChecker.Implementation.valueOf(c.getAuthorization());
		//AuthorizationChecker checker = authResolver.get(ctx.getContainer());
		final Route result;
		
		switch (authType) {			
			case OAUTH2: {
				result = Complete.notImplemented();
			}break;
			
			case MASTER_PWD: {
				
				final Config authConf = c.getAuthorizationConfig(); //XXX it's safe to tie AuthorizationConfig on Container instance ???
				
				if (StringUtils.equals(
					authConf.getString("masterPwd"), ctx.getAuthorization().orElseThrow(() -> new RuntimeException())
				)) {
					result = Complete.simple(UUID.randomUUID().toString());
				} else {
					result = Complete.forbidden();
				}
				
			}break;	
			
			case NO_AUTH: {
				result = Complete.methodNotAllowed();
			}break;		
			
			default: {
				result = null;
			}
		}		
		
		return result;
	}
	
}
