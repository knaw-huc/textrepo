package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nl.knaw.huc.service.document.metadata.DocumentMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"metadata"})
@Path("/rest/metadata")
public class MetadataResource {
  private static final Logger log = LoggerFactory.getLogger(MetadataResource.class);

  private final DocumentMetadataService documentMetadataService;

  public MetadataResource(DocumentMetadataService documentMetadataService) {
    this.documentMetadataService = requireNonNull(documentMetadataService);
  }

  @GET
  @Path("{key}/documents")
  @ApiOperation(
      value = "Find which documents have a given metadata key",
      tags = {"metadata", "documents"})
  @Produces(APPLICATION_JSON)
  public List<UUID> getDocumentsGivenMetadataKey(
      @PathParam("key")
      @ApiParam(required = true, example = "archive")
      @NotNull
          String key
  ) {
    log.debug("Get documents with metadata key: [{}]", key);
    return documentMetadataService.findByMetadataKey(key);
  }
}
