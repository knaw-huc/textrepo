package nl.knaw.huc.service.logging;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.function.Supplier;

public class LoggingService {

  private Supplier<UUID> uuidGenerator;

  public LoggingService(Supplier<UUID> uuidGenerator) {
    this.uuidGenerator = uuidGenerator;
  }

  public void startRequest() {
    MDC.put("request", "" + uuidGenerator.get());
  }

  public void stopRequest() {
    MDC.clear();
  }
}
