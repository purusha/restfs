package it.at.restfs.http.api;

import static akka.http.javadsl.server.Directives.completeOKWithFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.Route;
import it.at.restfs.auth.AuthorizationChecker;
import it.at.restfs.auth.AuthorizationChecker.Implementation;
import it.at.restfs.auth.AuthorizationConfigHandler;
import it.at.restfs.auth.AuthorizationManager;
import it.at.restfs.http.services.Complete;
import it.at.restfs.http.services.PathHelper.ContainerAuth;
import it.at.restfs.http.services.PathHelper.Request;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.dto.Container;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MangementController implements Controller {
	
	private final ContainerRepository cRepo;
	private final AuthorizationConfigHandler configResolver;
	private final MessageDispatcher dispatcher;
	private final AuthorizationManager authManager;
		
    public Route stats(Request t) {  
    	return withFuture(
			() -> cRepo.getStatistics(t.getContainer())
		);
    }

    public Route last(Request t) {
    	return withFuture(
			() -> cRepo.getCalls(t.getContainer())
		);
    }	

    //XXX do u rember Open-Close principle ???
    //XXX add test for all branch in this method
	public Route token(ContainerAuth ctx) {
    	final Container c = cRepo.load(ctx.getContainer());		
		final Implementation authType = AuthorizationChecker.Implementation.valueOf(c.getAuthorization());
		
		if (Implementation.OAUTH2 == authType) {
			return Complete.notImplemented(); //XXX call configured oAuth2 service end return a token generated!!!?
			
		} else if (Implementation.BASIC_AUTH == authType) {
			return Complete.methodNotAllowed();
			
		} else if (Implementation.NO_AUTH == authType) {
			return Complete.methodNotAllowed();
		} 
		
		//XXX only for MASTER_PWD
		final Config authConf = configResolver.get(c);
		
		if (ctx.getAuthorization().isPresent() && StringUtils.equals(
			authConf.getString(Implementation.MASTER_PWD.name()), ctx.getAuthorization().get()
		)) {				
			return withFuture(() -> token(authManager.generateTokenFor(c), Implementation.MASTER_PWD));
		} else {
			return Complete.forbidden();
		}
	}
	
	/*
	
		TOKEN endpoint
	
		se il container è configurato come MASTER_PWD 		
			> l'auth header deve contenere la master password della configurazione
			> il token generato deve essere conservato su file ( UUID ) e ritornato al client ( durata ?, num di token generabili ? )
		
		
		se il container è configurato come NO_AUTH l'endpoint andrà in errore		
		
		
		se il container è configurato come OAUTH2 ... ???			
	
	*/	
	
	//XXX code duplication of it.at.restfs.http.services.PerRequestContext.withFuture(Supplier<T>)
    //see https://doc.akka.io/docs/akka-http/current/handling-blocking-operations-in-akka-http-routes.html
    private <T> Route withFuture(Supplier<T> supplier) {
        return completeOKWithFuture(
            CompletableFuture.supplyAsync(supplier, dispatcher),
            Jackson.<T>marshaller()
        );        
    }
	
    //XXX extract a model for this
	private Map<String, String> token(String token, Implementation authType) {
		final HashMap<String, String> response = Maps.<String, String>newHashMap();
		
		response.put("token", token);
		response.put("ttl", String.valueOf(Integer.MAX_VALUE));
		response.put("type", authType.name());
		
		return response;
	}
	
}
