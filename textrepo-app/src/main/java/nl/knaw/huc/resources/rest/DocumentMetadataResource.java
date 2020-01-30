package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.service.DocumentMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
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
  public Map<String, String> getDocumentMetadata(
      @PathParam("docId") @NotNull @Valid UUID docId
  ) {
    return documentMetadataService.getByDocId(docId);
  }

  @PUT
  @Path("/{key}")
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Update value of a document metadata entry")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response updateMetadataEntry(
      @PathParam("docId") @NotNull @Valid UUID docId,
      @PathParam("key") @NotBlank String key,
      String value
  ) {
    logger.debug("updateMetadata: docId={}, key={}, value={}", docId, key, value);
    if (!documentMetadataService.update(docId, new MetadataEntry(key, value))) {
      throw new NotFoundException(format("No metadata field [%s] found for document [%s]", key, docId));
    }
    return Response.ok().build();
  }

}
