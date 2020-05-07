package nl.knaw.huc.service.logging;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import java.util.UUID;
import java.util.function.Supplier;

public class LoggingApplicationEventListener implements ApplicationEventListener {

  private final LoggingRequestEventListener loggingRequestEventListener;

  public LoggingApplicationEventListener(Supplier<UUID> uuidGenerator) {
    this.loggingRequestEventListener = new LoggingRequestEventListener(uuidGenerator);
  }

  @Override
  public void onEvent(ApplicationEvent event) {
  }

  @Override
  public RequestEventListener onRequest(RequestEvent requestEvent) {
    return loggingRequestEventListener;
  }
}
