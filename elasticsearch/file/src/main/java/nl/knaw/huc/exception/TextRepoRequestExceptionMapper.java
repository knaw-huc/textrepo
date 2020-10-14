package nl.knaw.huc.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class TextRepoRequestExceptionMapper implements ExceptionMapper<TextRepoRequestException> {

  private static final Logger log = LoggerFactory.getLogger(TextRepoRequestExceptionMapper.class);

  @Override
  public Response toResponse(TextRepoRequestException ex) {
    log.error(ex.getMessage(), ex);
    return Response
        .status(503)
        .entity(ex.getMessage())
        .build();
  }
}
