package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.ResultFileMetadataEntry;
import nl.knaw.huc.exceptions.MethodNotAllowedException;
import nl.knaw.huc.service.FileMetadataService;
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

@Api(tags = {"files", "metadata"})
@Path("/rest/files/{fileId}/metadata")
public class FileMetadataResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final String POST_ERROR_MSG = "Not allowed to post metadata: use put instead";

  private final FileMetadataService fileMetadataService;

  public FileMetadataResource(FileMetadataService fileMetadataService) {
    this.fileMetadataService = fileMetadataService;
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
  @ApiOperation(value = "Retrieve file metadata")
  @ApiResponses(value = {@ApiResponse(code = 200, responseContainer = "Map", response = String.class, message = "OK")})
  public Response get(
      @PathParam("fileId") @NotNull @Valid UUID fileId
  ) {
    logger.debug("get file metadata: fileId={}", fileId);
    return Response.ok(fileMetadataService.getMetadata(fileId)).build();
  }

  @PUT
  @Path("/{key}")
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create or update file metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response put(
      @PathParam("fileId") @Valid UUID fileId,
      @PathParam("key") @NotNull String key,
      @NotBlank String value
  ) {
    logger.debug("update or create file metadata: fileId={}, key={}, value={}", fileId, key, value);
    MetadataEntry entry = new MetadataEntry(key, value);
    fileMetadataService.upsert(fileId, entry);
    return Response.ok(new ResultFileMetadataEntry(fileId, entry)).build();
  }

  @DELETE
  @Path("/{key}")
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Delete document metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response delete(
      @PathParam("fileId") @NotNull @Valid UUID fileId,
      @PathParam("key") @NotBlank String key
  ) {
    logger.debug("delete file metadata: fileId={}, key={}", fileId, key);
    fileMetadataService.delete(fileId, key);
    return Response.ok().build();
  }

}
