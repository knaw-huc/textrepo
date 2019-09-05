package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.ResultFile;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.service.DocumentFileService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.api.TextRepoFile.fromContent;
import static nl.knaw.huc.resources.ResourceUtils.readContent;

@Api(tags = {"documents", "files"})
@Path("/documents/{uuid}/files")
public class DocumentFilesResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final DocumentFileService documentFileService;

  public DocumentFilesResource(DocumentFileService documentFileService) {
    this.documentFileService = documentFileService;
  }

  @PUT
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create a new document version by uploading a new file")
  @ApiResponses(value = {@ApiResponse(code = 200, response = Version.class, message = "OK")})
  public Response updateDocumentFile(
      @PathParam("uuid") @Valid UUID documentId,
      @FormDataParam("file") InputStream uploadedInputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail,
      @FormDataParam("file") FormDataBodyPart bodyPart
  ) {
    logger.debug("updateDocumentFile: documentId={}, file={}", documentId, fileDetail.getFileName());
    var resultFile = handleUpdate(
        documentId,
        readContent(uploadedInputStream),
        fileDetail.getFileName()
    );
    if (resultFile == null) {
      throw new NotFoundException("Could not replace document file");
    }
    return Response.ok(resultFile.getVersion()).build();
  }

  @GET
  @Timed
  @Produces(APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Download latest file of document")
  @ApiResponses(value = {@ApiResponse(code = 200, response = byte[].class, message = "OK")})
  public Response getFile(@PathParam("uuid") @Valid UUID documentId) {
    logger.debug("getFile: documentId={}", documentId);
    var file = documentFileService.getLatestFile(documentId);
    return Response
        .ok(file.getContent(), APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment;")
        .build();
  }

  /**
   * Try to update
   *
   * @return new version, or null if not replaced
   */
  private ResultFile handleUpdate(
      UUID documentId,
      byte[] content,
      String filename
  ) {
    logger.debug("replacing file: documentId={}", documentId);
    var file = fromContent(content);

    final var startReplacing = now();
    var version = documentFileService.replaceDocumentFile(documentId, file, filename);

    if (version.getDate().isBefore(startReplacing)) {
      logger.info("skip existing [{}]", filename);
      return null;
    }

    return new ResultFile(filename, version);
  }
}
