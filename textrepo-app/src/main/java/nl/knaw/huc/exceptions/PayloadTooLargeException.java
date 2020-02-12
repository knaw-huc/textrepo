package nl.knaw.huc.exceptions;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

public class PayloadTooLargeException extends ClientErrorException {
  public PayloadTooLargeException() {
    super(Response.Status.REQUEST_ENTITY_TOO_LARGE);
  }

  public PayloadTooLargeException(String msg) {
    super(msg, Response.Status.REQUEST_ENTITY_TOO_LARGE);
  }
}
