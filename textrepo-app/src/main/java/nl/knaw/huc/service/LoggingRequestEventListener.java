package nl.knaw.huc.service;

import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import java.util.UUID;
import java.util.function.Supplier;

public class LoggingRequestEventListener implements RequestEventListener {

  private final LoggingService loggingService;

  public LoggingRequestEventListener(Supplier<UUID> uuidGenerator) {
    this.loggingService = new LoggingService(uuidGenerator);
  }

  @Override
  public void onEvent(RequestEvent event) {
    switch (event.getType()) {
      case RESOURCE_METHOD_START:
        loggingService.startRequest();
        break;
      case FINISHED:
        loggingService.stopRequest();
        break;
      default:
        break;
    }
  }
}
