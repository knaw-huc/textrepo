package nl.knaw.huc.resources.task;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.HttpHeaders.LINK;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.resources.HeaderLink.Uri.CONTENTS;
import static nl.knaw.huc.resources.HeaderLink.Uri.DOCUMENT;
import static nl.knaw.huc.resources.HeaderLink.Uri.FILE;
import static nl.knaw.huc.resources.HeaderLink.Uri.VERSION;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import java.io.InputStream;
import java.net.URI;
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
import nl.knaw.huc.api.ResultImportDocument;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      "Create a new file version for document with {externalDocumentId} and file with {typeName}.",
      notes = "Use <code>allowNewDocument=true</code> "
          + "if you want to create a document when its externalId is not found.<br />"
          + "By default a new version is only created when no file versions exist with the same "
          + "contents.<br />"
          +
          "Use <code>asLatestVersion=true</code> to force the creation of a new version, "
          + "when its contents are different from the current latest version, "
          + "even when the new contents match those of older versions.<br />"
          + "Use <code>index=false</code> to skip indexing of file.<br />"
          + "See also the Concordion Integration Tests &gt; \"Task API\".",
      tags = {"task", "import", "documents", "files", "versions", "contents"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Contents found in earlier version",
          response = ResultImportDocument.class,
          responseHeaders = {
              @ResponseHeader(name = LINK, response = URI.class, description = LINK_DESCRIPTION)}),
      @ApiResponse(code = 201, message = "New version created for contents",
          response = ResultImportDocument.class,
          responseHeaders = {
              @ResponseHeader(name = LINK, response = URI.class, description = LINK_DESCRIPTION),
              @ResponseHeader(name = LOCATION, response = URI.class, description =
                  LOCATION_DESCRIPTION)}),
      @ApiResponse(code = 404, message = "When allowNewDocument=false and no document is found "
          + "for externalId")})
  public Response importDocumentContentsForFileWithType(
      @PathParam("externalId")
      @ApiParam(example = "document_1234", required = true)
      @NotBlank
      String externalId,
      @PathParam("typeName")
      @ApiParam(example = "plaintext", required = true)
      @NotBlank
      String typeName,
      @QueryParam("allowNewDocument")
      @DefaultValue("false")
      boolean allowNewDocument,
      @QueryParam("asLatestVersion")
      @DefaultValue("false")
      boolean asLatestVersion,
      @QueryParam("index")
      @DefaultValue("true")
      boolean index,
      @NotNull
      @FormDataParam("contents")
      InputStream uploadedInputStream,
      @NotNull
      @FormDataParam("contents")
      FormDataContentDisposition fileDetail
  ) {
    log.debug(
        "Importing document contents for file with type: "
            + "externalId={}, typeName={}, allowNewDocument={}, asLatestVersion={}",
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
               .withIndexing(index)
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
