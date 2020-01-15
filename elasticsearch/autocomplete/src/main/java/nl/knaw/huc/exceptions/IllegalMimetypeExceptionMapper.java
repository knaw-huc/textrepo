package nl.knaw.huc.exceptions;

import nl.knaw.huc.AutocompleteApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class IllegalMimetypeExceptionMapper implements ExceptionMapper<IllegalMimetypeException> {

  private static final Logger logger = LoggerFactory.getLogger(IllegalMimetypeExceptionMapper.class);

  @Override
  public Response toResponse(IllegalMimetypeException exception) {
    logger.error(exception.getMessage());
    return Response
        .status(422)
        .entity(exception.getMessage())
        .build();
  }
}
