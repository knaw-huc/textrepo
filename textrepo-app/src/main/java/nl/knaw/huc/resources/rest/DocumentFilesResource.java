package nl.knaw.huc.resources.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Tag;
import nl.knaw.huc.api.FormPageParams;
import nl.knaw.huc.api.ResultPage;
import nl.knaw.huc.api.ResultTextRepoFile;
import nl.knaw.huc.core.PageParams;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huc.helpers.Paginator.toResult;

@Api(tags = {"documents", "files"})
@Path("/rest/documents/{docId}/files")
public class DocumentFilesResource {

  private static final Logger log = LoggerFactory.getLogger(DocumentFilesResource.class);
  private final DocumentFilesService documentFilesService;
  private final Paginator paginator;

  private static class ResultTextRepoFilePage extends ResultPage<ResultTextRepoFile> {}

  public DocumentFilesResource(
      DocumentFilesService documentFilesService,
      Paginator paginator
  ) {
    this.documentFilesService = requireNonNull(documentFilesService);
    this.paginator = requireNonNull(paginator);
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve document files")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultTextRepoFilePage.class, message = "OK")})
  public Response getDocumentFiles(
      @PathParam("docId")
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @Valid
          UUID docId,
      @QueryParam("typeId")
      @ApiParam(example = "1")
          Short typeId,
      @BeanParam
          FormPageParams pageParams
  ) {
    log.debug("Get document files: docId={}; typeId={}; pageParams={}", docId, typeId, pageParams);

    final var params = paginator.fromForm(pageParams);
    final var page = documentFilesService.getFilesByDocumentAndTypeId(docId, typeId, params);
    final var result = toResult(page, (TextRepoFile file) -> new ResultTextRepoFile(docId, file));

    log.debug("Got document files: {}", page);
    return Response
        .ok(result)
        .build();
  }
}
