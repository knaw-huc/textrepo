package nl.knaw.huc.resources.rest;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Map;
import java.util.UUID;
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
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.ResultDocumentMetadataEntry;
import nl.knaw.huc.exceptions.MethodNotAllowedException;
import nl.knaw.huc.service.document.metadata.DocumentMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = {"documents", "metadata"})
@Path("/rest/documents/{docId}/metadata")
public class DocumentMetadataResource {

  private static final Logger log = LoggerFactory.getLogger(DocumentMetadataResource.class);
  private static final String POST_ERROR_MSG = "Not allowed to post metadata: use put instead";

  private final DocumentMetadataService documentMetadataService;

  public DocumentMetadataResource(
      DocumentMetadataService documentMetadataService
  ) {
    this.documentMetadataService = requireNonNull(documentMetadataService);
  }

  @POST
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = POST_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = POST_ERROR_MSG)})
  public Response postDocumentMetadataIsNotAllowed() {
    throw new MethodNotAllowedException(POST_ERROR_MSG);
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve document metadata")
  public Map<String, String> getDocumentMetadata(
      @PathParam("docId")
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @NotNull
      @Valid
      UUID docId
  ) {
    log.debug("Get document metadata: docId={}", docId);
    var metadata = documentMetadataService.getByDocId(docId);
    log.debug("Got document metadata: {}", metadata);
    return metadata;
  }

  @PUT
  @Path("/{key}")
  @Timed
  @Consumes(TEXT_PLAIN)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create or update document metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response putDocumentMetadataEntry(
      @PathParam("docId")
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @NotNull
      @Valid
      UUID docId,
      @PathParam("key")
      @NotBlank
      @ApiParam(required = true, example = "archive")
      String key,
      @ApiParam(required = true, example = "a.1.2.3")
      @NotNull
      String value
  ) {
    log.debug("Update metadata: docId={}, key={}, value={}", docId, key, value);
    var entry = new MetadataEntry(key, value);
    documentMetadataService.upsert(docId, entry);
    log.debug("Updated metadata: docId={}, entry={}", docId, entry);
    return Response.ok(new ResultDocumentMetadataEntry(docId, entry)).build();
  }

  @DELETE
  @Path("/{key}")
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Delete document metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response deleteDocumentMetadataEntry(
      @PathParam("docId")
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @NotNull
      @Valid
      UUID docId,
      @PathParam("key")
      @ApiParam(required = true, example = "archive")
      @NotBlank
      String key
  ) {
    log.debug("Delete metadata: docId={}, key={}", docId, key);
    documentMetadataService.delete(docId, key);
    log.debug("Deleted metadata");
    return Response.ok().build();
  }

}
