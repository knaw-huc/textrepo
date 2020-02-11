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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.resources.ResourceUtils.readContent;

@Api(tags = {"task", "import"})
@Path("task/import")
public class ImportResource {
  private static final Logger LOG = LoggerFactory.getLogger(ImportResource.class);
  private final TaskBuilderFactory factory;
  private final int maxPayloadSize;

  public ImportResource(TaskBuilderFactory factory, int maxPayloadSize) {
    this.factory = factory;
    this.maxPayloadSize = maxPayloadSize;
    LOG.debug("ImportFileResource configured with maxPayloadSize={}", maxPayloadSize);
  }

  @POST
  @Path("documents/{externalId}/{type}")
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation("Import file as the current version for {type} of document referenced by {externalId}")
  @ApiResponses(value = {@ApiResponse(code = 201, message = "CREATED")})
  public Response importDocumentContentsForFileWithType(
      @NotBlank @PathParam("externalId") String externalId,
      @NotBlank @PathParam("type") String type,
      @NotNull @FormDataParam("contents") InputStream uploadedInputStream,
      @NotNull @FormDataParam("contents") FormDataContentDisposition fileDetail
  ) {
    final var announcedSize = fileDetail.getSize();
    LOG.debug("ImportFile: externalId={}, type={}, size={}", externalId, type, announcedSize);

    final var builder = factory.getDocumentImportBuilder();
    final var importTask = builder.forExternalId(externalId)
                                  .withType(type)
                                  .forFilename(fileDetail.getFileName())
                                  .withContents(readContent(uploadedInputStream, maxPayloadSize))
                                  .build();

    // TODO: what would be a good (generic) return value for a task?
    importTask.run();

    return Response.ok().build();
  }
}
