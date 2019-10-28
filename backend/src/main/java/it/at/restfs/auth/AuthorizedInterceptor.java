package it.at.restfs.auth;

import static it.at.restfs.http.services.Complete.forbidden;

import java.util.Arrays;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ClassUtils;

import com.google.inject.Inject;

import it.at.restfs.http.services.PathHelper.ContainerAuth;

public class AuthorizedInterceptor implements MethodInterceptor {
	
	@Inject
	private AuthorizationManager authManager;   
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {		
		final ContainerAuth ctx = (ContainerAuth) Arrays.stream(invocation.getArguments())
			.filter(x -> ClassUtils.isAssignable(x.getClass(), ContainerAuth.class))
			.findFirst()
			.get();
			
		if (! authManager.isTokenValidFor(ctx.getAuthorization(), ctx.getContainer())) {
        	return forbidden();
        } else {
        	return invocation.proceed();
        }
	}

}
