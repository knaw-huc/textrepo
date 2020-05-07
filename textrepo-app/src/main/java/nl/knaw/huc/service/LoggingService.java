package nl.knaw.huc.service;

import org.slf4j.MDC;

import java.util.UUID;

public class LoggingService {

  public static void startRequest() {
    MDC.put("request", "" + UUID.randomUUID());
  }

  public static void stopRequest() {
    MDC.clear();
  }
}
