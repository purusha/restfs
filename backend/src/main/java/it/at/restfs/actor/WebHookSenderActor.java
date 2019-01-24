package it.at.restfs.actor;

import java.nio.file.Path;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import com.google.inject.Inject;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.pattern.PatternsCS;
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
    private final ActorSelection cleanUp;
    
    @Inject
    public WebHookSenderActor(Http http, ContainerRepository cRepo) {        
        this.http = http;
        this.cRepo = cRepo;
        this.cleanUp = getContext().system().actorSelection("/user/" + CleanupActor.ACTOR);
        
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
            .POST("http://requestbin.fullcontact.com/yumxfnyu") //XXX container config
            .withEntity(ContentTypes.parse("text/vnd.yaml"), p);

        //XXX no retry if from remote service receive an error 
        final CompletionStage<Path> stage = http
            .singleRequest(request)
            .thenApply((HttpResponse r) -> {       
                LOGGER.info("status {}", r.status());
                
                return p;
            });
        
        PatternsCS
            .pipe(stage, getContext().dispatcher())
            .to(cleanUp);            
        
    }

}
