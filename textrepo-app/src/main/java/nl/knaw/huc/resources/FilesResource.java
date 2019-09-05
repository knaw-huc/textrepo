package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.service.FileService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.net.URI;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.resources.ResourceUtils.readContent;

@Api(tags = {"files"})
@Path("/files")
public class FilesResource {
  private final Logger logger = LoggerFactory.getLogger(FilesResource.class);
  private final FileService fileService;

  public FilesResource(FileService fileService) {
    this.fileService = fileService;
  }

  @POST
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create new file")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultFile.class, message = "OK")})
  public Response postFile(
      @FormDataParam("file") InputStream uploadedInputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail
  ) {

    final var file = TextRepoFile.fromContent(readContent(uploadedInputStream));

    fileService.addFile(file);

    return Response.created(locationOf(file))
                   .entity(new ResultFile(file))
                   .build();
  }

  @GET
  @Path("/{sha224}")
  @Timed
  @Produces(APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Download file by sha224")
  @ApiResponses(value = {@ApiResponse(code = 200, response = byte[].class, message = "OK")})
  public Response getFileBySha224(@PathParam("sha224") String sha224) {
    if (sha224.length() != 56) {
      logger.warn("bad length in sha224 ({}): {}", sha224.length(), sha224);
      throw new BadRequestException("not a sha224: " + sha224);
    }

    final var file = fileService.getBySha224(sha224);

    return Response.ok(file.getContent(), APPLICATION_OCTET_STREAM)
                   .header("Content-Disposition", "attachment;")
                   .build();
  }

  private static URI locationOf(TextRepoFile file) {
    return UriBuilder.fromResource(FilesResource.class)
                     .path("{sha224}")
                     .build(file.getSha224());
  }

  private static class ResultFile {
    private final TextRepoFile file;

    private ResultFile(TextRepoFile file) {
      this.file = file;
    }

    @JsonProperty
    public String getSha224() {
      return file.getSha224();
    }
  }

}
