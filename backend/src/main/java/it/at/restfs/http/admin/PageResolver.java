package it.at.restfs.http.admin;

import static it.at.restfs.http.services.Complete.internalError;
import static it.at.restfs.http.services.Complete.textHtml;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.jknack.handlebars.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import akka.http.javadsl.server.Route;
import it.at.restfs.storage.ContainerRepository;
import it.at.restfs.storage.dto.Container;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton    
@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class PageResolver {
    
    private final TemplateResover resolver;
    private final ContainerRepository cRepo;
    
    public Route dashboard(String baseUri) {
        return inner(
            "dashboard", baseUri,
            "dashboard-data", cRepo.getDashboardData()
        );
    }
    
    public Route allContainer(String baseUri) {
        return inner(
            "all-container", baseUri,
            "containers", cRepo.findAll().stream().collect(Collectors.toMap(Container::getId, Container::getName))
        );
    }       
    
    public Route newContainer(String baseUri) {
        return inner(
            "new-container", baseUri
        );
    }         

    public Route getContainer(String baseUri, UUID containerId) {
        return inner(
            "get-container", baseUri,
            "container", cRepo.load(containerId),
            "containerStatistics", cRepo.getStatistics(containerId),
            "containerCalls", cRepo.getCalls(containerId)
        );
    }
    
    private Route inner(String templateName, String baseUri, Object... kv) {
        final Map<String, Object> map = new HashMap<String, Object>();        
        for(int i = 0; i < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i+1]);
        }
        
        final Context context = context(baseUri, map);
        
        try {            
            return textHtml(resolver.get(templateName).apply(context));                    
        } catch (Exception e) {
            LOGGER.error("pageresolver inner error: ", e);
            return internalError();                    
        }
    }
    
    private Context context(String baseUri, Map<String, Object> data) {
        data.put("baseUri", baseUri); //this is used as base path of all static files (img, css and js)            

        return Context.newContext(data);        
    }                
}