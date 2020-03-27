package it.at.restfs;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import akka.actor.ActorSystem;
import akka.actor.Props;
import it.at.restfs.actor.CleanupActor;
import it.at.restfs.actor.EventHandlerActor;
import it.at.restfs.actor.MachineStatusInfoActor;
import it.at.restfs.actor.WebHookSenderActor;
import it.at.restfs.guice.GuiceActorUtils;
import it.at.restfs.guice.GuiceExtension;
import it.at.restfs.guice.GuiceExtensionImpl;
import it.at.restfs.http.AdminHTTPListener;
import it.at.restfs.http.HTTPListener;
import it.at.restfs.storage.RootFileSystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class RestFsApplication {
	
    private final ActorSystem system;
    private final Injector injector;
	private final RootFileSystem rfs;

    @Inject
    public RestFsApplication(Injector injector, ActorSystem system, RootFileSystem rfs) {
        this.injector = injector;
        this.system = system;
		this.rfs = rfs;
    }
    
    public void run() {
    	if (rfs.isUseful()) {
    		LOGGER.info("Root folder is {}", rfs.getRoot());
            system.registerExtension(GuiceExtension.provider);

            //configure Guice inside Akka
            final GuiceExtensionImpl guiceExtension = GuiceExtension.provider.get(system);
            guiceExtension.setInjector(injector);
            
            //start actors
            system.actorOf(build(CleanupActor.class), CleanupActor.ACTOR);         
            system.actorOf(build(EventHandlerActor.class), EventHandlerActor.ACTOR);
            system.actorOf(build(WebHookSenderActor.class), WebHookSenderActor.ACTOR);
            system.actorOf(build(MachineStatusInfoActor.class));
            
            //start http endpoint        
            injector.getInstance(AdminHTTPListener.class);        
            injector.getInstance(HTTPListener.class);
            
            LOGGER.info("-------------------------------------------------");
            LOGGER.info("   >    RestFS STARTED                           ");
            LOGGER.info("-------------------------------------------------");
    		
    	} else {

            LOGGER.info("-------------------------------------------------");
            LOGGER.info("   >    RestFS NOT STARTED                       ");
            LOGGER.info("-------------------------------------------------");
    		
    	}
    }
    
    private Props build(Class<?> clazz) {
        return GuiceActorUtils.makeProps(system, clazz);
    }   
}
