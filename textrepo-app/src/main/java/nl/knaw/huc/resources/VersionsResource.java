package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.service.VersionService;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api
@Path("/documents/{uuid}/versions")
public class VersionsResource {
  private VersionService versionService;

  public VersionsResource(VersionService versionService) {
    this.versionService = versionService;
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get all versions of a document")
  @ApiResponses(value = {@ApiResponse(code = 200, responseContainer = "Map", response = Version.class, message = "OK")})
  public Response getVersions(
      @PathParam("uuid") @Valid UUID documentId
  ) {
    return Response.ok().entity(versionService.getVersions(documentId)).build();
  }

}
