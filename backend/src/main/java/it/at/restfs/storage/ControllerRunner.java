package it.at.restfs.storage;

import static akka.http.javadsl.server.Directives.complete;
import static it.at.restfs.http.PathResolver.getPathString;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.reflections.ReflectionUtils;

import com.google.common.base.Predicate;
import com.google.inject.Inject;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.Route;
import it.at.restfs.event.Event;
import it.at.restfs.http.AuthorizationManager;
import it.at.restfs.http.Controller;
import it.at.restfs.http.HTTPListener.Request;
import it.at.restfs.http.PerRequestContext;
import it.at.restfs.http.PerRequestContext.Factory;
import lombok.SneakyThrows;

public class ControllerRunner {

    private final Map<HttpMethod, Controller> mapping;
    private final AuthorizationManager authManager;     
    private final ContainerRepository cRepo;
	private final Factory factory;
	
    @Inject
    public ControllerRunner(
        Map<HttpMethod, Controller> mapping,
        AuthorizationManager authManager,
        ContainerRepository cRepo,
        PerRequestContext.Factory factory
    ) {
        this.mapping = mapping;
        this.authManager = authManager;
        this.cRepo = cRepo;
		this.factory = factory;
    }
    
    //XXX this method should be moved into a Controller ?
    //XXX and should be executed in a Future ?
    public Route stats(UUID container, String authorization) {        
        if (! authManager.isTokenValidFor(authorization, container)) {
            return complete(StatusCodes.FORBIDDEN);
        }
        
        return complete(
            StatusCodes.OK, 
            cRepo.load(container).getStatistics(), 
            Jackson.<Map<Integer, Long>>marshaller()
        );
    }

    //XXX this method should be moved into a Controller ?
    //XXX and should be executed in a Future ?    
    public Route last(UUID container, String authorization) {        
        if (! authManager.isTokenValidFor(authorization, container)) {
            return complete(StatusCodes.FORBIDDEN);
        }
        
        return complete(
            StatusCodes.OK, 
            cRepo.getCalls(container), 
            Jackson.<List<Event>>marshaller()
        );
    }
        
	@SneakyThrows(Throwable.class)
	public Route handler(UUID container, String authorization, Uri uri, HttpMethod method, String operation) {        
        if (! authManager.isTokenValidFor(authorization, container)) {
            return complete(StatusCodes.FORBIDDEN);
        }
        
        final Controller controller = mapping.get(method);
        
        if (Objects.isNull(controller)) {
        	complete(StatusCodes.METHOD_NOT_ALLOWED);
        }
        
        final Request request = new Request(container, getPathString(uri) , operation);
        
        try {
        	final Field field = resolveField(controller.getClass());        	        	        	
        	field.set(controller, factory.create(request));
        	
            return (Route) controller.getClass().getDeclaredMethod(request.getOperation().toLowerCase(), Request.class).invoke(controller, request);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
            throw e;
        } catch (InvocationTargetException e) { //XXX important ... because we use Reflection !!!?
            throw e.getCause();
        }
    }

	//XXX per queste cose ci sarebbe la pena di morte !!!?
	@SuppressWarnings("unchecked")
	private Field resolveField(Class<? extends Controller> class1) {
		//final Field field = controller.getClass().getDeclaredField("x"); 
		
		final Field field = ReflectionUtils.getFields(class1, new Predicate<Field>() {
			@Override
			public boolean apply(Field field) {
				return field.getType().equals(PerRequestContext.class);
			}
		}).iterator().next();
		
		field.setAccessible(true);
		
		return field;
	}   
	
}
