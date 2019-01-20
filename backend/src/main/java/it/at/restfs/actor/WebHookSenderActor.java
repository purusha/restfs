package it.at.restfs.actor;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import com.google.inject.Inject;
import akka.actor.ActorRef;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpRequest;
import it.at.restfs.guice.GuiceAbstractActor;
import it.at.restfs.storage.ContainerRepository;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

@Slf4j
public class WebHookSenderActor extends GuiceAbstractActor {
    
    public static final String ACTOR = "WebHookSender";
    private static final String UP = "clean-up";
    private static final FiniteDuration SCHEDULE = FiniteDuration.apply(1, TimeUnit.MINUTES);
    
    private final ContainerRepository cRepo;
    private final Http http;
    
    @Inject
    public WebHookSenderActor(Http http, ContainerRepository cRepo) {        
        this.http = http;
        this.cRepo = cRepo;
        
        getContext().system().scheduler().schedule(
            SCHEDULE, SCHEDULE, getSelf(), UP, getContext().system().dispatcher(), ActorRef.noSender()
        );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .matchEquals(UP, m -> {

                cRepo.findAll()
                    .forEach(
                        container -> cRepo.getWebhook(container).forEach(this::makeRequest)
                    ); 
                
                //XXX ri-schedule from here !!?
                
            })
            .matchAny(this::unhandled)
            .build();
    }

    private void makeRequest(Path p) {
        final HttpRequest request = HttpRequest
            .POST("http://requestbin.fullcontact.com/1bott5m1") //XXX container config
            .withEntity(ContentTypes.parse("text/vnd.yaml"), p);
        
        http
            .singleRequest(request)        
            .thenAccept(httpResp -> {
                LOGGER.info("status {}", httpResp.status());
                
                /*
                    
                    actor handler will do this step:
                    
                        if send is OK remove files
                        
                        else retry for 3 times...
                        
                        if not ... remove files
                
                 */                
                
            });
    }

}
