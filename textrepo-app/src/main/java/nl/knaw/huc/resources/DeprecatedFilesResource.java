package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormContents;
import nl.knaw.huc.api.MultipleLocations;
import nl.knaw.huc.api.ResultContents;
import nl.knaw.huc.api.ResultVersion;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.service.FileService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.resources.ResourceUtils.locationOf;
import static nl.knaw.huc.resources.ResourceUtils.readContent;
import static nl.knaw.huc.resources.ZipHandling.handleZipFile;
import static nl.knaw.huc.resources.ZipHandling.isZip;
import static org.eclipse.jetty.util.StringUtil.isBlank;

/**
 * TODO: deprecated, or move to /task?
 */
@Api(tags = {"files"})
@Path("/files")
public class DeprecatedFilesResource {
  private static final Logger LOG = LoggerFactory.getLogger(DeprecatedFilesResource.class);

  private final FileService fileService;
  private final int maxPayloadSize;

  public DeprecatedFilesResource(FileService fileService, int maxPayloadSize) {
    this.fileService = fileService;
    this.maxPayloadSize = maxPayloadSize;

    LOG.debug("FileContentsResource configured with maxPayloadSize={}", maxPayloadSize);
  }

  @POST
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create new file by uploading a file or create multiple files by uploading a zip")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "OK"),
      @ApiResponse(code = 200, response = MultipleLocations.class, message = "OK")})
  public Response addFile(
      @NotNull @FormDataParam("type") String typeName,
      @FormDataParam("contents") InputStream inputStream,
      @FormDataParam("contents") FormDataContentDisposition fileDetail,
      @FormDataParam("contents") FormDataBodyPart bodyPart
  ) {
    if (isBlank(fileDetail.getFileName())) {
      throw new IllegalStateException("No filename");
    }

    LOG.debug("addFile: type={}, filename={}", typeName, fileDetail.getFileName());

    if (isZip(bodyPart, fileDetail)) {
      var resultFiles = handleZipFile(inputStream, formContents -> handleNewFile(typeName, formContents));
      return Response
          .ok(new MultipleLocations(resultFiles))
          .build();
    }

    var resultFile = handleNewFile(
        typeName,
        new FormContents(
            fileDetail.getFileName(),
            readContent(inputStream, maxPayloadSize)
        )
    );

    return Response
        .created(locationOf(resultFile.getVersion().getFileId()))
        .build();
  }

  @GET
  @Path("/{uuid}/latest")
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get latest version of file")
  @ApiResponses(value = {@ApiResponse(code = 200, response = Version.class, message = "OK")})
  public Response getLatestVersionOfFile(
      @PathParam("uuid") @NotNull @Valid UUID fileId
  ) {
    LOG.debug("getLatestVersionOfFile: fileId={}", fileId);
    var version = fileService.getLatestVersion(fileId);
    return Response.ok(version).build();
  }

  private ResultContents handleNewFile(String typeName, FormContents formContents) {
    var fileId = fileService.createFile(typeName, formContents.getName());
    var version = fileService.createVersion(
        fileId,
        formContents.getContent()
    );
    return new ResultContents(
        formContents.getName(),
        new ResultVersion(version)
    );
  }

}