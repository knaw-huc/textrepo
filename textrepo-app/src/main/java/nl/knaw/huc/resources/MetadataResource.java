package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.service.MetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"files", "metadata"})
@Path("/files/{uuid}/metadata")
public class MetadataResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final MetadataService metadataService;

  public MetadataResource(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @POST
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create metadata for a file")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response addMetadata(
      @PathParam("uuid") @Valid UUID fileId,
      Map<String, String> metadata
  ) {
    logger.debug("addMetadata: fileId={}, metadata={}", fileId, metadata);
    metadataService.addMetadata(fileId, metadata);
    return Response.ok().build();
  }

  @PUT
  @Path("/{key}")
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Update value of a file metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response updateMetadataEntry(
      @PathParam("uuid") @Valid UUID fileId,
      @PathParam("key") @Valid String key,
      String value
  ) {
    logger.debug("updateMetadata: fileId={}, key={}, value={}", fileId, key, value);
    metadataService.update(fileId, new MetadataEntry(key, value));
    return Response.ok().build();
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get all metadata of a file")
  @ApiResponses(value = {@ApiResponse(code = 200, responseContainer = "Map", response = String.class, message = "OK")})
  public Response getMetadata(@PathParam("uuid") @Valid UUID fileId) {
    logger.debug("getMetadata: fileId={}", fileId);
    return Response.ok(metadataService.getMetadata(fileId)).build();
  }

}
