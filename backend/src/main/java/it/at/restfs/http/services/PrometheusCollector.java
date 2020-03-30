package it.at.restfs.http.services;

import static it.at.restfs.http.services.PathHelper.APP_NAME;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class PrometheusCollector {	
	
	private static final Counter requestsTotals = Counter.build()
	     .name(APP_NAME + "_requests_total").help("Total requests.")
	     .register();
    
	private static final Counter containerRequests2xx = Counter.build()
		.name(APP_NAME + "_container_requests2xx").help("Container requests 2xx.")
		.labelNames("container")
		.register();                  	

	private static final Counter containerRequests4xx = Counter.build()
		.name(APP_NAME + "_container_requests4xx").help("Container requests 4xx.")
		.labelNames("container")
		.register();                    	
	private static final Counter containerRequests5xx = Counter.build()
		.name(APP_NAME + "_container_requests5xx").help("Container requests 5xx.")
		.labelNames("container")
		.register();                    		
	
	private final Optional<HTTPServer> server;
		
	@Inject
	public PrometheusCollector(Config config) {
		HTTPServer s = null;
		
		if (config.hasPath("restfs.http.prometheus")) {			
	        final String host = config.getString("restfs.http.prometheus.interface");
	        final int port = config.getInt("restfs.http.prometheus.port");
			
	        try {
	        	DefaultExports.initialize();
	        	
				s = new HTTPServer(host, port);
				
		        LOGGER.info("");
		        LOGGER.info("Expose following Metrics endpoint");
		        LOGGER.info("http://" + host + ":" + port + "/metrics");
		        LOGGER.info("");
				
			} catch (IOException e) {
				LOGGER.error("", e);
			}	       
		}	
		
        server = Optional.ofNullable(s);		
	}	
	
    //XXX call this please when application shutdown
    public void shutdown() {
    	server.ifPresent(s -> s.stop());	
    }
    
    public void metrics(UUID container, int httpStatus, int reqNumber) {
    	server.ifPresent(s -> {
	    	requestsTotals.inc(reqNumber);
	    	
	    	if (httpStatus >= 200 && httpStatus < 300) {
	    		containerRequests2xx.labels(container.toString()).inc(reqNumber);
	    	} else if (httpStatus >= 400 && httpStatus < 500) {
	    		containerRequests4xx.labels(container.toString()).inc(reqNumber);
	    	} else if (httpStatus >= 500 && httpStatus < 600) {
	    		containerRequests5xx.labels(container.toString()).inc(reqNumber);
	    	}
    	});
    }
    
}
