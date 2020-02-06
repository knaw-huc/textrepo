package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.ResultDocument;
import nl.knaw.huc.api.ResultTextrepoFile;
import nl.knaw.huc.service.DocumentFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"documents"})
@Path("/rest/documents/{id}/files")
public class DocumentFilesResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final DocumentFilesService documentFilesService;

  public DocumentFilesResource(
      DocumentFilesService documentFilesService
  ) {
    this.documentFilesService = documentFilesService;
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve document")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response get(
      @PathParam("id") @Valid UUID id
  ) {
    logger.debug("get files for document: id={}", id);
    final var files = documentFilesService
        .getFilesByDocumentId(id);
    var results = files
        .stream()
        .map((file) -> new ResultTextrepoFile(id, file))
        .collect(toList());
    return Response
        .ok(results)
        .build();
  }
}
