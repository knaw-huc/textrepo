package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.service.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/documents/{uuid}/versions")
public class VersionsResource {
  private VersionService versionService;

  public VersionsResource(VersionService versionService) {
    this.versionService = versionService;
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  public Response getVersions(
      @PathParam("uuid") @Valid UUID documentId
  ) {
    return Response.ok().entity(versionService.getVersions(documentId)).build();
  }

}
