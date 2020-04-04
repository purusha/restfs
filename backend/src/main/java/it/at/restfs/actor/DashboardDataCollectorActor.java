package it.at.restfs.actor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Stopwatch;
import com.google.inject.Inject;

import akka.actor.ActorRef;
import it.at.restfs.guice.GuiceAbstractActor;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.RootFileSystem;
import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.storage.dto.AssetType;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

@Slf4j
public class DashboardDataCollectorActor extends GuiceAbstractActor {
	
	public static final String ACTOR = "DashboardDataCollector";
	
	/*
		XXX this implementation make a count of containers that has been configured with Storage.FS 
		XXX use a linux command to do its work
		XXX save last N execution ... for build dashboard with wide range execution data
	 */
	
	private final static String DO = "do";
	
	private final ContainerRepository cRepo;
	private final RootFileSystem rfs;
	
	@Inject
    public DashboardDataCollectorActor(ContainerRepository cRepo, RootFileSystem rfs) {
		 this.cRepo = cRepo;
		 this.rfs = rfs;
		 
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
            		.map(c -> {
            			return new ContainerData(
        					c.getId(), count(c.getId(), AssetType.FILE), count(c.getId(), AssetType.FOLDER), cRepo.getStatistics(c.getId())
    					);
            		})
            		.collect(Collectors.toList());
            	            	
            	cRepo.saveDashboardData(data);

            	stopwatch.stop();
            	
            	LOGGER.info("{} write data in {}", this.getClass().getSimpleName(), stopwatch); 
            	
            })
            .matchAny(this::unhandled)
            .build();            
	}
	
	/*

		try using tree command instead of find
		
		$ tree <directory>
		
		3 directories, 3 files		
		
	 */
	
	@SneakyThrows
	private Long count(UUID id, AssetType asset) {		
		final String path = rfs.containerPath(id, AbsolutePath.EMPTY).toFile().getAbsolutePath();		
		final String resourceType =  AssetType.FILE == asset ? "f" : "d";

		final String[] params = { "/bin/sh", "-c", "find " + path + " -type " + resourceType + " | wc -l" };
		final Process process = Runtime.getRuntime().exec(params);
		
		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		final Long count = Long.valueOf(reader.readLine().trim());
		reader.close();
		
		return count;
	}

	@Getter
	public static class ContainerData {
		
		private final UUID containerId;
		private final Long files;
		private final Long folders;
		private final Map<Integer, Long> stats;		
		
	    @JsonCreator
	    public ContainerData(
	        @JsonProperty("containerId") UUID containerId, 
	        @JsonProperty("files") Long files,
	        @JsonProperty("folders") Long folders,
	        @JsonProperty("stats") Map<Integer, Long> stats
	    ) {
	    	this.containerId = containerId;
	    	this.files = files;
	    	this.folders = folders;
	    	this.stats = stats;
	    }    
		
	}

}
