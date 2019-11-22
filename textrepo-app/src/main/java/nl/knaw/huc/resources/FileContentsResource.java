package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.ResultContents;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.service.FileContentsService;
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
import static nl.knaw.huc.core.Contents.fromContent;
import static nl.knaw.huc.resources.ResourceUtils.readContent;

@Api(tags = {"files", "contents"})
@Path("/files/{uuid}/contents")
public class FileContentsResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final FileContentsService fileContentsService;

  public FileContentsResource(FileContentsService fileContentsService) {
    this.fileContentsService = fileContentsService;
  }

  @PUT
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create a new file version by uploading a new file")
  @ApiResponses(value = {@ApiResponse(code = 200, response = Version.class, message = "OK")})
  public Response updateFileContents(
      @PathParam("uuid") @Valid UUID fileId,
      @FormDataParam("contents") InputStream uploadedInputStream,
      @FormDataParam("contents") FormDataContentDisposition fileDetail,
      @FormDataParam("contents") FormDataBodyPart bodyPart
  ) {
    logger.debug("updateFileContents: fileId={}, file={}", fileId, fileDetail.getFileName());
    var resultFile = handleUpdate(
        fileId,
        readContent(uploadedInputStream),
        fileDetail.getFileName()
    );
    if (resultFile == null) {
      throw new NotFoundException("Could not replace file contents");
    }
    return Response.ok(resultFile.getVersion()).build();
  }

  @GET
  @Timed
  @Produces(APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Download latest file contents")
  @ApiResponses(value = {@ApiResponse(code = 200, response = byte[].class, message = "OK")})
  public Response getContents(@PathParam("uuid") @Valid UUID fileId) {
    logger.debug("getContents: fileId={}", fileId);
    var contents = fileContentsService.getLatestFile(fileId);
    return Response
        .ok(contents.getContent(), APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment;")
        .build();
  }

  /**
   * Try to update
   *
   * @return new version, or null if not replaced
   */
  private ResultContents handleUpdate(
      UUID fileId,
      byte[] content,
      String filename
  ) {
    logger.debug("replacing contents: fileId={}", fileId);
    var contents = fromContent(content);

    final var startReplacing = now();
    var version = fileContentsService.replaceFileContents(fileId, contents, filename);

    if (version.getDate().isBefore(startReplacing)) {
      logger.info("skip existing [{}]", filename);
      return null;
    }

    return new ResultContents(filename, version);
  }
}
