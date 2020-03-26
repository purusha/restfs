package it.at.restfs.actor;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import com.google.inject.Inject;
import akka.actor.ActorRef;
import it.at.restfs.guice.GuiceAbstractActor;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

@Slf4j
public class MachineStatusInfoActor extends GuiceAbstractActor {    
	private final static String MACHINE_STATUS = "machine-status";

	private final OperatingSystemMXBean osBean;
	private final Runtime runtime;
	
	@Inject
	public MachineStatusInfoActor() {
		this.osBean = ManagementFactory.getOperatingSystemMXBean();
		this.runtime = Runtime.getRuntime();
		
		getContext().system().scheduler().scheduleWithFixedDelay(
			FiniteDuration.apply(10, TimeUnit.SECONDS),
			FiniteDuration.apply(120, TimeUnit.SECONDS),
			getSelf(), MACHINE_STATUS, getContext().system().dispatcher(), ActorRef.noSender()
		);
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.matchEquals(MACHINE_STATUS, m -> {
				
				LOGGER.info("############## MACHINE STATUS ################");
				for (Method method : osBean.getClass().getDeclaredMethods()) {
					method.setAccessible(true);

					logPublicMethodGet(method);
				}		
				
				LOGGER.info("############## RUNTIME ################");
				LOGGER.info("  totalMemory = {}", runtime.totalMemory());
				LOGGER.info("  freeMemory = {}", runtime.freeMemory());		
				LOGGER.info("##############################################");
				
			})
            .matchAny(this::unhandled)
            .build();
	}
	
	private void logPublicMethodGet(Method method) {
		if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
            Object value;
            try {
                value = method.invoke(osBean);

                if (value instanceof Long) {
                    value = MessageFormat.format("{0}", value);
                }
            } catch (Exception e) {
                value = e;
            }

            LOGGER.info("  {} = {}", method.getName(), value);
        }
	}	
}
