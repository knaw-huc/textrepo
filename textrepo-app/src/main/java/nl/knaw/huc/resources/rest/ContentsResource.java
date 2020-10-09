package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.service.contents.ContentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static nl.knaw.huc.helpers.ContentsHelper.getContentsAsAttachment;

@Api(tags = {"contents"})
@Path("/rest/contents")
public class ContentsResource {

  private static final Logger log = LoggerFactory.getLogger(ContentsResource.class);
  private final ContentsService contentsService;
  private final int decompressLimit;

  public ContentsResource(ContentsService contentsService, int contentDecompressionLimit) {
    this.contentsService = contentsService;
    this.decompressLimit = contentDecompressionLimit;
    log.debug("contentDecompressionLimit={}", decompressLimit);
  }

  @GET
  @Path("/{sha}")
  @Timed
  @Produces({APPLICATION_OCTET_STREAM, APPLICATION_JSON})
  @ApiOperation(value = "Retrieve contents")
  @ApiResponses(value = {@ApiResponse(code = 200, response = byte[].class, message = "OK")})
  public Response get(
      @PathParam("sha") @NotBlank String sha
  ) {
    log.debug("Get contents: sha={}", sha);

    if (sha.length() != 56) {
      log.warn("Bad length in sha ({}): {}", sha.length(), sha);
      throw new BadRequestException("not a sha: " + sha);
    }

    final var contents = contentsService.getBySha(sha);

    log.debug("Got contents: {}", contents);

    return getContentsAsAttachment(contents, decompressLimit).build();
  }

}
