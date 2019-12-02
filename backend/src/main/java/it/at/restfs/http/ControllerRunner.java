package it.at.restfs.http;

import static it.at.restfs.http.services.Complete.methodNotAllowed;
import static it.at.restfs.http.services.PathHelper.build;
import static it.at.restfs.http.services.PathHelper.getPathString;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;

import com.google.inject.Inject;
import com.google.inject.Injector;

import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.Route;
import it.at.restfs.auth.Authorized;
import it.at.restfs.auth.NotAuthorized;
import it.at.restfs.http.services.PathHelper.ContainerAuth;
import it.at.restfs.http.services.PathHelper.Request;
import it.at.restfs.storage.dto.AbsolutePath;
import it.at.restfs.http.services.PerRequestContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ControllerRunner {
	
	private final PerRequestContext.Factory factory;	
	private final MangementController mController;
	private final Injector injector;
    
	@Authorized
    public Route stats(ContainerAuth ctx) {        
		return mController.stats(build(ctx.getContainer(), AbsolutePath.of("stats"), null));
    }

	@Authorized	
    public Route last(ContainerAuth ctx) {  
		return mController.last(build(ctx.getContainer(), AbsolutePath.of("last"), null));
    }

	@NotAuthorized
    public Route token(ContainerAuth ctx) {     
		return mController.token(ctx);
    }
	
	@Authorized
	@SneakyThrows(Throwable.class)
	public Route handler(ContainerAuth ctx, Uri uri, HttpMethod method, String operation) {        
        final RunningData data = RUN_CONTEXT.get(method);
		final Controller controller = injector.getInstance(data.getCClazz());
        
        if (Objects.isNull(controller)) {
        	return methodNotAllowed();
        }
        
        try {
        	final Request request = build(ctx.getContainer(), getPathString(uri) , operation);
        			        	        	
        	data.getPerReq().set(controller, factory.create(request));        	
        	LOGGER.debug("will call operation {}", request.getOperation());
        	
        	return (Route) data.getOperations().stream()
				.filter(m -> StringUtils.equalsIgnoreCase(m.getName(), request.getOperation()))
				.findFirst().get()
				.invoke(controller, request);
        	
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchElementException | SecurityException e) {
            throw e;
        } catch (InvocationTargetException e) { //XXX important ... because we use Reflection !!!?
            throw e.getCause();
        }
    }
	
	@Getter
	@RequiredArgsConstructor
	private static class RunningData {
		
		private final Class<? extends Controller> cClazz;
		private final Field perReq;
		private final List<Method> operations;
		
	}
		
	final static Map<HttpMethod, RunningData> RUN_CONTEXT = new HashMap<HttpMethod, RunningData>() {
		private static final long serialVersionUID = -7910997263891218171L;

		{
			
			put(HttpMethods.GET, resolve(GetController.class));
			put(HttpMethods.POST, resolve(PostController.class));
			put(HttpMethods.PUT, resolve(PutController.class));
			put(HttpMethods.DELETE, resolve(DeleteController.class));
			
			//don't add MangementController to this data
		}

		@SuppressWarnings("unchecked")
		private RunningData resolve(Class<? extends Controller> class1) {
			final Field field = ReflectionUtils.getFields(class1, new com.google.common.base.Predicate<Field>() {
				@Override
				public boolean apply(Field field) {
					return field.getType().equals(PerRequestContext.class);
				}
			}).iterator().next();
			
			field.setAccessible(true);
						
			final List<Method> operations = Arrays.stream(class1.getDeclaredMethods())
				.filter((input) -> input.getModifiers() == Modifier.PUBLIC)
				.filter((input) -> ReflectionUtils.withReturnType(Route.class).apply(input))
				.filter((input) -> ReflectionUtils.withParametersCount(1).apply(input) && input.getParameterTypes()[0].equals(Request.class))
				.collect(Collectors.toList());		
			
			LOGGER.info("for {} found {} field and {} operation method", class1.getSimpleName(), field.getName(), operations.size());
			
			return new RunningData(class1, field, operations);
		}
	};	
	
}
