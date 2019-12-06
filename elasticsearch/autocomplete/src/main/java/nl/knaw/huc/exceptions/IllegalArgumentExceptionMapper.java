package nl.knaw.huc.exceptions;

import nl.knaw.huc.AutocompleteApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

  private static final Logger logger = LoggerFactory.getLogger(AutocompleteApplication.class);

  @Override
  public Response toResponse(IllegalArgumentException exception) {
    exception.printStackTrace();
    return Response
        .status(422)
        .entity(exception.getMessage())
        .build();
  }
}
