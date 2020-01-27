package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

@Api(tags = {"import", "documents"})
@Path("task/import")
public class Import {
  private static final Logger LOG = LoggerFactory.getLogger(Import.class);
  private final TaskBuilderFactory factory;

  public Import(TaskBuilderFactory factory) {
    this.factory = factory;
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
    LOG.debug("importDocumentContentsForFileWithType: externalId={}, type={}", externalId, type);
    final var builder = factory.getDocumentImportBuilder();

    final var importTask = builder.forExternalId(externalId)
                                  .withType(type)
                                  .forFilename(fileDetail.getName())
                                  .withContents(uploadedInputStream)
                                  .build();

    // TODO: what would be a good (generic) return value for a task?
    importTask.run();

    return Response.ok().build();
  }
}
