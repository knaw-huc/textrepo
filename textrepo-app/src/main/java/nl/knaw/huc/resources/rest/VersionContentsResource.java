package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.ResultVersion;
import nl.knaw.huc.service.VersionContentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

@Api(tags = {"versions", "contents"})
@Path("/rest/versions/{versionId}/contents")
public class VersionContentsResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final String POST_ERROR_MSG = "Not allowed to post content: create new version instead";
  private static final String PUT_ERROR_MSG = "Not allowed to put content of version: create new version instead";
  private static final String DELETE_ERROR_MSG = "Not allowed to delete content of version: delete version instead";
  private static final String[] ALLOWED = {"GET"};

  private VersionContentsService contentsService;

  public VersionContentsResource(VersionContentsService contentsService) {
    this.contentsService = contentsService;
  }

  @POST
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = POST_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = POST_ERROR_MSG)})
  public Response post() {
    throw new NotAllowedException(POST_ERROR_MSG, ALLOWED);
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve version content")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultVersion.class, message = "OK")})
  public Response get(
      @PathParam("versionId") @NotNull @Valid UUID versionId
  ) {
    logger.debug("get version content: versionId={}", versionId);
    var content = contentsService.getByVersionId(versionId);
    return Response
        .ok(content.getContent(), APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment;")
        .build();
  }

  @PUT
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = PUT_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = PUT_ERROR_MSG)})
  public Response put() {
    throw new NotAllowedException(PUT_ERROR_MSG, ALLOWED);
  }

  @DELETE
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = DELETE_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = DELETE_ERROR_MSG)})
  public Response delete() {
    throw new NotAllowedException(DELETE_ERROR_MSG, ALLOWED);
  }

}
