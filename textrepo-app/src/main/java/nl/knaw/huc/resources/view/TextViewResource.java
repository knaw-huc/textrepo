package nl.knaw.huc.resources.view;

import nl.knaw.huc.core.Contents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

public class TextViewResource {
  private static final Logger log = LoggerFactory.getLogger(TextViewResource.class);

  private final Contents contents;

  public TextViewResource(Contents contents) {
    this.contents = contents;
  }

  @GET
  @Path("{startOffset}/{endOffset}")
  public Response getCharRange(
      @PathParam("endOffset") @NotNull int endOffset,
      @PathParam("startOffset") @NotNull int startOffset
  ) {
    log.debug("getCharRange: sha224={}, range=[{}:{}]", contents.getSha224(), startOffset, endOffset);
    return Response.ok().build();
  }
}
