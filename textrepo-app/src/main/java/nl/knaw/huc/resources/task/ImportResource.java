package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.resources.ResourceUtils.readContents;

@Api(tags = {"task", "import"})
@Path("task/import")
public class ImportResource {

  private static final Logger log = LoggerFactory.getLogger(ImportResource.class);
  private final TaskBuilderFactory factory;
  private final int maxPayloadSize;

  public ImportResource(TaskBuilderFactory factory, int maxPayloadSize) {
    this.factory = factory;
    this.maxPayloadSize = maxPayloadSize;
    log.info("ImportFileResource configured with maxPayloadSize={}", maxPayloadSize);
  }

  @POST
  @Path("documents/{externalId}/{typeName}")
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation("Import file as the current version for {typeName} of document referenced by {externalId} " +
      "without indexing")
  @ApiResponses(value = {@ApiResponse(code = 201, message = "CREATED")})
  public Response importDocumentContentsForFileWithType(
      @NotBlank @PathParam("externalId") String externalId,
      @NotBlank @PathParam("typeName") String typeName,
      @QueryParam("allowNewDocument") @DefaultValue("false") boolean allowNewDocument,
      @NotNull @FormDataParam("contents") InputStream uploadedInputStream,
      @NotNull @FormDataParam("contents") FormDataContentDisposition fileDetail
  ) {
    log.debug(
        "Import document contents for file with type: " +
        "externalId={}, " +
        "typeName={}, " +
        "allowNewDocument={}",
        externalId, typeName, allowNewDocument
    );

    final var builder = factory.getDocumentImportBuilder();
    final var importTask = builder.allowNewDocument(allowNewDocument)
                                  .forExternalId(externalId)
                                  .withTypeName(typeName)
                                  .forFilename(fileDetail.getFileName())
                                  .withContents(readContents(uploadedInputStream, maxPayloadSize))
                                  .build();

    importTask.run();
    log.debug("Imported document contents");
    return Response.ok().build();
  }
}
