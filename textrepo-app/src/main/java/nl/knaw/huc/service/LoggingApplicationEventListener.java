package nl.knaw.huc.service;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.annotation.Priority;
import javax.ws.rs.ext.Provider;
import java.util.UUID;
import java.util.function.Supplier;

@Provider
@Priority(10000) // This needs to be the last listener to run
public class LoggingApplicationEventListener implements ApplicationEventListener {

  private final LoggingRequestEventListener loggingRequestEventListener;

  public LoggingApplicationEventListener(Supplier<UUID> uuidGenerator) {
    this.loggingRequestEventListener = new LoggingRequestEventListener(uuidGenerator);
  }

  @Override
  public void onEvent(ApplicationEvent event) {}

  @Override
  public RequestEventListener onRequest(RequestEvent requestEvent) {
    return loggingRequestEventListener;
  }
}
