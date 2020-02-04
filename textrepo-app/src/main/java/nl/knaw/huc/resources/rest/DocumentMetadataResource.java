package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.ResultDocumentMetadataEntry;
import nl.knaw.huc.service.DocumentMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"documents", "metadata"})
@Path("/rest/documents/{docId}/metadata")
public class DocumentMetadataResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final DocumentMetadataService documentMetadataService;

  public DocumentMetadataResource(
      DocumentMetadataService documentMetadataService
  ) {
    this.documentMetadataService = documentMetadataService;
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Map<String, String> get(
      @PathParam("docId") @NotNull @Valid UUID docId
  ) {
    logger.debug("get metadata: docId={}", docId);
    return documentMetadataService.getByDocId(docId);
  }

  @PUT
  @Path("/{key}")
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create or update document metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response update(
      @PathParam("docId") @NotNull @Valid UUID docId,
      @PathParam("key") @NotBlank String key,
      String value
  ) {
    logger.debug("update metadata: docId={}, key={}, value={}", docId, key, value);
    var entry = new MetadataEntry(key, value);
    documentMetadataService.upsert(docId, entry);
    return Response.ok(new ResultDocumentMetadataEntry(docId, entry)).build();
  }

  @DELETE
  @Path("/{key}")
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Update value of a document metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response delete(
      @PathParam("docId") @NotNull @Valid UUID docId,
      @PathParam("key") @NotBlank String key
  ) {
    logger.debug("delete metadata: docId={}, key={}", docId, key);
    var entry = new MetadataEntry(key, null);
    documentMetadataService.delete(docId, entry);
    return Response.ok().build();
  }

}