package it.at.restfs.actor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Stopwatch;
import com.google.inject.Inject;

import akka.actor.ActorRef;
import it.at.restfs.guice.GuiceAbstractActor;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.StorageResolver;
import it.at.restfs.storage.dto.AssetType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

@Slf4j
public class DashboardDataCollectorActor extends GuiceAbstractActor {
	
	public static final String ACTOR = "DashboardDataCollector";
	
	/*
		TODO save last N execution ... for build dashboard with wide range execution data
	 */
	
	private final static String DO = "do";
	
	private final ContainerRepository cRepo;
	private final StorageResolver resolver;
	
	@Inject
    public DashboardDataCollectorActor(ContainerRepository cRepo, StorageResolver resolver) {
		 this.cRepo = cRepo;
		 this.resolver = resolver;
		 
		 getContext().system().scheduler().scheduleWithFixedDelay(
			 FiniteDuration.Zero(), FiniteDuration.apply(1, TimeUnit.MINUTES),
			 getSelf(), DO, getContext().system().dispatcher(), ActorRef.noSender()
		 );		 
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
            .matchEquals(DO, m -> {
            	
            	final Stopwatch stopwatch = Stopwatch.createStarted();            	
            	
            	final List<ContainerData> data = cRepo.findAll().stream()
        			.map(
    					c -> Pair.of(c.getId(), resolver.get(c.getId()))
        			)
        			.map(
    					pair -> new ContainerData(
							pair.getLeft(), 
							pair.getRight().count(pair.getLeft(), AssetType.FILE), 
							pair.getRight().count(pair.getLeft(), AssetType.FOLDER)
						)
					)
            		.collect(Collectors.toList());
            	            	
            	cRepo.saveDashboardData(data);

            	stopwatch.stop();
            	
            	LOGGER.info("{} write data in {}", this.getClass().getSimpleName(), stopwatch); 
            	
            })
            .matchAny(this::unhandled)
            .build();            
	}
	
	@Getter
	public static class ContainerData {		
		private final UUID containerId;
		private final Long files;
		private final Long folders;
		
	    @JsonCreator
	    public ContainerData(
	        @JsonProperty("containerId") UUID containerId, 
	        @JsonProperty("files") Long files,
	        @JsonProperty("folders") Long folders
	    ) {
	    	this.containerId = containerId;
	    	this.files = files;
	    	this.folders = folders;
	    }    		
	}

}
