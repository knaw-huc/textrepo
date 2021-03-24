package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.helpers.ContentsHelper;
import nl.knaw.huc.service.contents.ContentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

@Api(tags = {"contents"})
@Path("/rest/contents")
public class ContentsResource {

  private static final Logger log = LoggerFactory.getLogger(ContentsResource.class);
  private final ContentsService contentsService;
  private final ContentsHelper contentsHelper;

  public ContentsResource(ContentsService contentsService, ContentsHelper contentsHelper) {
    this.contentsService = requireNonNull(contentsService);
    this.contentsHelper = requireNonNull(contentsHelper);
  }

  @GET
  @Path("/{sha}")
  @Timed
  @Produces({APPLICATION_OCTET_STREAM, APPLICATION_JSON})
  @ApiOperation(value = "Retrieve contents")
  @ApiResponses(value = {@ApiResponse(code = 200, response = byte[].class, message = "OK")})
  public Response getContents(
      @HeaderParam(ACCEPT_ENCODING) String acceptEncoding,
      @PathParam("sha") @NotBlank String sha
  ) {
    log.debug("Get contents: sha={}", sha);

    if (sha.length() != 56) {
      log.warn("Bad length in sha ({}): {}", sha.length(), sha);
      throw new BadRequestException("not a sha: " + sha);
    }

    final var contents = contentsService.getBySha(sha);

    log.debug("Got contents: {}", contents);

    return contentsHelper.asAttachment(contents, acceptEncoding)
                         .header(CONTENT_TYPE, APPLICATION_OCTET_STREAM)
                         .build();
  }

}
