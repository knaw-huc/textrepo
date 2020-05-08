package nl.knaw.huc.service.logging;

import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Set and clear request UUID before and after each request
 */
public class LoggingRequestEventListener implements RequestEventListener {

  private Supplier<UUID> uuidGenerator;

  public LoggingRequestEventListener(Supplier<UUID> uuidGenerator) {
    this.uuidGenerator = uuidGenerator;
  }

  @Override
  public void onEvent(RequestEvent event) {
    switch (event.getType()) {
      case RESOURCE_METHOD_START:
        MDC.put("request", "" + uuidGenerator.get());
        break;
      case FINISHED:
        MDC.clear();
        break;
      default:
        break;
    }
  }
}
