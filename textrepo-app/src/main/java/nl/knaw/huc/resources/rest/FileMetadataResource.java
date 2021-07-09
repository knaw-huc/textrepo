package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ExampleProperty;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.ResultFileMetadataEntry;
import nl.knaw.huc.exceptions.MethodNotAllowedException;
import nl.knaw.huc.service.file.metadata.FileMetadataService;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Api(tags = {"files", "metadata"})
@Path("/rest/files/{fileId}/metadata")
public class FileMetadataResource {

  private static final Logger log = LoggerFactory.getLogger(FileMetadataResource.class);
  private static final String POST_ERROR_MSG = "Not allowed to post metadata: use put instead";

  private final FileMetadataService fileMetadataService;

  public FileMetadataResource(FileMetadataService fileMetadataService) {
    this.fileMetadataService = requireNonNull(fileMetadataService);
  }

  @POST
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = POST_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = POST_ERROR_MSG)})
  public Response postFileMetadataIsNotAllowed() {
    throw new MethodNotAllowedException(POST_ERROR_MSG);
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve file metadata")
  @ApiResponses(value = {@ApiResponse(code = 200, responseContainer = "Map", response = String.class, message = "OK")})
  public Response getFileMetadata(
      @PathParam("fileId")
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @NotNull
      @Valid
          UUID fileId
  ) {
    log.debug("Get file metadata: fileId={}", fileId);
    var metadata = fileMetadataService.getMetadata(fileId);
    log.debug("Got file metadata: {}", metadata);
    return Response.ok(metadata).build();
  }

  @PUT
  @Path("/{key}")
  @Timed
  @Consumes(TEXT_PLAIN)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create or update file metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response putFileMetadataEntry(
      @PathParam("fileId")
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @Valid
          UUID fileId,
      @PathParam("key")
      @ApiParam(required = true, example = "tool")
      @NotNull
          String key,
      @NotNull
          String value
  ) {
    log.debug("Update or create file metadata: fileId={}, key={}, value={}", fileId, key, value);
    var entry = new MetadataEntry(key, value);
    fileMetadataService.upsert(fileId, entry);
    log.debug("Updated or created file metadata");
    return Response.ok(new ResultFileMetadataEntry(fileId, entry)).build();
  }

  @DELETE
  @Path("/{key}")
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Delete file metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response deleteFileMetadataEntry(
      @PathParam("fileId")
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @NotNull
      @Valid
          UUID fileId,
      @PathParam("key")
      @ApiParam(example = "tool")
      @NotBlank
          String key
  ) {
    log.debug("Delete file metadata: fileId={}, key={}", fileId, key);
    fileMetadataService.delete(fileId, key);
    log.debug("Deleted file metadata");
    return Response.ok().build();
  }

}
