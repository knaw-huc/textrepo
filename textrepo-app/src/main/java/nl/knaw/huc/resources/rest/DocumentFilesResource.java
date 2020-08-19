package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormPageParams;
import nl.knaw.huc.api.ResultDocument;
import nl.knaw.huc.api.ResultTextRepoFile;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.service.document.files.DocumentFilesService;
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
import static nl.knaw.huc.helpers.Paginator.toResult;

@Api(tags = {"documents"})
@Path("/rest/documents/{docId}/files")
public class DocumentFilesResource {

  private static final Logger log = LoggerFactory.getLogger(DocumentFilesResource.class);
  private final DocumentFilesService documentFilesService;
  private final Paginator paginator;

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
    log.debug("Get document files: docId={}; pageParams={}", docId, pageParams);

    final var page = documentFilesService.getFilesByDocumentId(docId, paginator.fromForm(pageParams));
    var result = toResult(page, (TextRepoFile file) -> new ResultTextRepoFile(docId, file));

    log.debug("Got document files: {}", page);
    return Response
        .ok(result)
        .build();
  }
}
