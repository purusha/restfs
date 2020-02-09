package it.at.restfs;

import java.io.File;

import org.apache.commons.lang3.tuple.Pair;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Getter;

public class IntegrationResolver {

    private static final Config TEST_CONF = ConfigFactory.parseFile(new File(IntegrationResolver.class.getResource("/test.conf").getPath()));
    
    @Getter
    private final OSFeatures features;
    
    @Getter
    private final Pair<String, Integer> publicEndpoint;
    
    @Getter
    private final Pair<String, Integer> adminEndpoint;
    
    public IntegrationResolver() {            	
        publicEndpoint = Pair.of(TEST_CONF.getString("http.public.host"), TEST_CONF.getInt("http.public.port"));        
        adminEndpoint = Pair.of(TEST_CONF.getString("http.admin.host"), TEST_CONF.getInt("http.admin.port"));        
        features = OSFeatures.build();        
    }
	
}
