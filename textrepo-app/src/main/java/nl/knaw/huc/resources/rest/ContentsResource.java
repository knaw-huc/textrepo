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
@Path("/contents")
public class ContentsResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final ContentsService contentsService;

  public ContentsResource(ContentsService contentsService) {
    this.contentsService = contentsService;
  }

  @GET
  @Path("/{sha224}")
  @Timed
  @Produces(APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Download contents by sha224")
  @ApiResponses(value = {@ApiResponse(code = 200, response = byte[].class, message = "OK")})
  public Response getContentsBySha224(
      @PathParam("sha224") @NotBlank String sha224
  ) {
    logger.debug("getContentsBySha224: sha224={}", sha224);
    if (sha224.length() != 56) {
      logger.warn("bad length in sha224 ({}): {}", sha224.length(), sha224);
      throw new BadRequestException("not a sha224: " + sha224);
    }

    final var contents = contentsService.getBySha224(sha224);

    return Response
        .ok(contents.getContent(), APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment;")
        .build();
  }

}
