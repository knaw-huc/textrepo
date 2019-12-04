package nl.knaw.huc.resources;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.service.FileService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.resources.ResourceUtils.readContent;

@Path("/documents")
public class DocumentsResource {
  private static final Logger LOG = LoggerFactory.getLogger(DocumentsResource.class);

  private final FileService fileService;

  private DocumentsResource(FileService fileService) {
    this.fileService = fileService;
  }

  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @POST
  @ApiOperation(value = "Create a new document by uploading contents for one of the document's file types")
  @ApiResponses(value = {@ApiResponse(code = 200, response = Version.class, message = "OK")})
  public Response addDocument(
      @FormDataParam("type") @Nonnull String type,
      @FormDataParam("contents") InputStream uploadedInputStream,
      @FormDataParam("contents") FormDataContentDisposition fileDetail,
      @FormDataParam("contents") FormDataBodyPart bodyPart
  ) {
    final var newFileId = fileService.createFile(type);
    LOG.debug("New file created with fileId={}", newFileId);

    final var version = fileService.createVersionWithFilenameMetadata(
        newFileId,
        readContent(uploadedInputStream),
        fileDetail.getFileName());
    LOG.debug("New version created: {}", version);

    return Response.ok(version).build();
  }

}
