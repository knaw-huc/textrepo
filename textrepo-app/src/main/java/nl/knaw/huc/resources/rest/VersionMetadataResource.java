package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.ResultVersionMetadataEntry;
import nl.knaw.huc.exceptions.MethodNotAllowedException;
import nl.knaw.huc.service.version.metadata.VersionMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Api(tags = {"versions", "metadata"})
@Path("/rest/versions/{versionId}/metadata")
public class VersionMetadataResource {

  private static final Logger log = LoggerFactory.getLogger(VersionMetadataResource.class);
  private static final String POST_ERROR_MSG = "Not allowed to post metadata: use put instead";

  private final VersionMetadataService versionMetadataService;

  public VersionMetadataResource(VersionMetadataService versionMetadataService) {
    this.versionMetadataService = versionMetadataService;
  }

  @POST
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = POST_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = POST_ERROR_MSG)})
  public Response post() {
    throw new MethodNotAllowedException(POST_ERROR_MSG);
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve version metadata")
  @ApiResponses(value = {@ApiResponse(code = 200, responseContainer = "Map", response = String.class, message = "OK")})
  public Response get(
      @PathParam("versionId") @NotNull @Valid UUID versionId
  ) {
    log.debug("Get version metadata: versionId={}", versionId);
    var metadata = versionMetadataService.getMetadata(versionId);
    log.debug("Got version metadata: {}", metadata);
    return Response.ok(metadata).build();
  }

  @PUT
  @Path("/{key}")
  @Timed
  @Consumes(TEXT_PLAIN)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create or update version metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response put(
      @PathParam("versionId") @Valid UUID versionId,
      @PathParam("key") @NotNull String key,
      @NotBlank String value
  ) {
    log.debug("Update or create version metadata: versionId={}, key={}, value={}", versionId, key, value);
    var entry = new MetadataEntry(key, value);
    versionMetadataService.upsert(versionId, entry);
    log.debug("Updated or created version metadata");
    return Response.ok(new ResultVersionMetadataEntry(versionId, entry)).build();
  }

  @DELETE
  @Path("/{key}")
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Delete document metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response delete(
      @PathParam("versionId") @NotNull @Valid UUID versionId,
      @PathParam("key") @NotBlank String key
  ) {
    log.debug("Delete version metadata: versionId={}, key={}", versionId, key);
    versionMetadataService.delete(versionId, key);
    log.debug("Deleted version metadata");
    return Response.ok().build();
  }

}
