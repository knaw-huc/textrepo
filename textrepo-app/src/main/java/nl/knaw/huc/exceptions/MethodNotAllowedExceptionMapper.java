package nl.knaw.huc.exceptions;

import nl.knaw.huc.api.ResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED;

public class MethodNotAllowedExceptionMapper implements ExceptionMapper<MethodNotAllowedException> {

  private static final Logger logger = LoggerFactory.getLogger(MethodNotAllowedExceptionMapper.class);

  @Override
  public Response toResponse(MethodNotAllowedException ex) {
    logger.debug(ex.getMessage());
    return Response
        .status(METHOD_NOT_ALLOWED)
        .entity(new ResultException(METHOD_NOT_ALLOWED.getStatusCode(), ex.getMessage()))
        .build();
  }
}
