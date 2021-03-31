package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import nl.knaw.huc.api.ResultImportDocument;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.InputStream;
import java.net.URI;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.HttpHeaders.LINK;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.resources.HeaderLink.Uri.CONTENTS;
import static nl.knaw.huc.resources.HeaderLink.Uri.DOCUMENT;
import static nl.knaw.huc.resources.HeaderLink.Uri.FILE;
import static nl.knaw.huc.resources.HeaderLink.Uri.VERSION;

@Api(tags = {"task", "import"})
@Path("task/import")
public class ImportResource {
  private static final Logger log = LoggerFactory.getLogger(ImportResource.class);

  private static final String LINK_DESCRIPTION =
      "REST URIs of document, file, version, and contents used in this request";
  private static final String LOCATION_DESCRIPTION = "(absolute) URL of newly created version";

  private final TaskBuilderFactory factory;

  public ImportResource(TaskBuilderFactory factory) {
    this.factory = requireNonNull(factory);
  }

  @POST
  @Path("documents/{externalId}/{typeName}")
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value =
      "Import file contents to create version for type {typeName} of document {externalDocumentId} (without indexing)",
      notes = "Use allowNewDocument=true if document {externalId} is not yet in use and you want it to be created. " +
          "Use asLatestVersion=true if contents you are uploading are already present as an earlier version, " +
          "but you need those contents to represent that latest version; a new version node will be added. " +
          "See also the Concordion Integration Tests under \"Task API\"")

  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Contents found in earlier version",
          response = ResultImportDocument.class,
          responseHeaders = {@ResponseHeader(name = LINK, response = URI.class, description = LINK_DESCRIPTION)}),
      @ApiResponse(code = 201, message = "New version created for contents",
          response = ResultImportDocument.class,
          responseHeaders = {
              @ResponseHeader(name = LINK, response = URI.class, description = LINK_DESCRIPTION),
              @ResponseHeader(name = LOCATION, response = URI.class, description = LOCATION_DESCRIPTION)}),
      @ApiResponse(code = 404, message = "When allowNewDocument=false and no document is found for externalId")})
  public Response importDocumentContentsForFileWithType(
      @NotBlank @PathParam("externalId") @ApiParam(example = "document_1234") String externalId,
      @NotBlank @PathParam("typeName") @ApiParam(example = "plaintext") String typeName,
      @QueryParam("allowNewDocument") @DefaultValue("false") boolean allowNewDocument,
      @QueryParam("asLatestVersion") @DefaultValue("false") boolean asLatestVersion,
      @NotNull @FormDataParam("contents") InputStream uploadedInputStream,
      @NotNull @FormDataParam("contents") FormDataContentDisposition fileDetail
  ) {
    log.debug(
        "Importing document contents for file with type: " +
            "externalId={}, " +
            "typeName={}, " +
            "allowNewDocument={}, " +
            "asLatestVersion={}",
        externalId, typeName, allowNewDocument, asLatestVersion
    );

    final var importTask =
        factory.getDocumentImportBuilder()
               .allowNewDocument(allowNewDocument)
               .asLatestVersion(asLatestVersion)
               .forExternalId(externalId)
               .withTypeName(typeName)
               .forFilename(fileDetail.getFileName())
               .withContents(uploadedInputStream)
               .build();

    final var result = importTask.run();
    log.debug("Imported document contents: {}", result);

    final ResponseBuilder builder;
    if (result.isNewVersion()) {
      builder = Response.created(VERSION.build(result.getVersionId()));
    } else {
      builder = Response.ok();
    }

    return builder.entity(result)
                  .link(CONTENTS.build(result.getContentsSha()), "contents")
                  .link(DOCUMENT.build(result.getDocumentId()), "document")
                  .link(FILE.build(result.getFileId()), "file")
                  .link(VERSION.build(result.getVersionId()), "version")
                  .build();
  }
}
