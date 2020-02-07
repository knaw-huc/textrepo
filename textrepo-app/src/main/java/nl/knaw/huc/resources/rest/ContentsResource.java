package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.service.ContentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

@Api(tags = {"contents"})
@Path("/rest/contents")
public class ContentsResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final ContentsService contentsService;

  public ContentsResource(ContentsService contentsService) {
    this.contentsService = contentsService;
  }

  @GET
  @Path("/{sha}")
  @Timed
  @Produces(APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Retrieve contents")
  @ApiResponses(value = {@ApiResponse(code = 200, response = byte[].class, message = "OK")})
  public Response getContentsBySha224(
      @PathParam("sha") @NotBlank String sha
  ) {
    logger.debug("getContentsBySha224: sha={}", sha);
    if (sha.length() != 56) {
      logger.warn("bad length in sha ({}): {}", sha.length(), sha);
      throw new BadRequestException("not a sha: " + sha);
    }

    final var contents = contentsService.getBySha(sha);

    return Response
        .ok(contents.getContent(), APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment;")
        .build();
  }

}
