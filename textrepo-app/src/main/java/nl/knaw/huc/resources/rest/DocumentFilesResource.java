package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormPageParams;
import nl.knaw.huc.api.ResultDocument;
import nl.knaw.huc.api.ResultTextrepoFile;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.service.DocumentFilesService;
import nl.knaw.huc.service.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huc.service.Paginator.mapPage;

@Api(tags = {"documents"})
@Path("/rest/documents/{docId}/files")
public class DocumentFilesResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final DocumentFilesService documentFilesService;
  private Paginator paginator;

  public DocumentFilesResource(
      DocumentFilesService documentFilesService,
      Paginator paginator
  ) {
    this.documentFilesService = documentFilesService;
    this.paginator = paginator;
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve document files")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Response get(
      @PathParam("docId") @Valid UUID docId,
      @BeanParam FormPageParams pageParams
  ) {
    logger.debug("get files for document: docId={}", docId);
    final var page = documentFilesService.getFilesByDocumentId(docId, paginator.withDefaults(pageParams));
    var result = mapPage(page, (TextrepoFile file) -> new ResultTextrepoFile(docId, file));
    return Response
        .ok(result)
        .build();
  }
}
