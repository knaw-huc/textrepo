package nl.knaw.huc.resources.rest;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import nl.knaw.huc.exceptions.MethodNotAllowedException;
import nl.knaw.huc.helpers.ContentsHelper;
import nl.knaw.huc.service.version.content.VersionContentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = {"versions", "contents"})
@Path("/rest/versions/{versionId}/contents")
public class VersionContentsResource {

  private static final Logger log = LoggerFactory.getLogger(VersionContentsResource.class);
  private static final String POST_ERROR_MSG =
      "Not allowed to post contents: post new version instead";
  private static final String PUT_ERROR_MSG =
      "Not allowed to put contents of version: post new version instead";
  private static final String DELETE_ERROR_MSG =
      "Not allowed to delete contents of version: delete version instead";

  private final VersionContentsService contentsService;
  private final ContentsHelper contentsHelper;

  public VersionContentsResource(VersionContentsService contentsService,
                                 ContentsHelper contentsHelper) {
    this.contentsService = requireNonNull(contentsService);
    this.contentsHelper = requireNonNull(contentsHelper);
  }

  @POST
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = POST_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = POST_ERROR_MSG)})
  public Response postVersionContentsIsNotAllowed() {
    throw new MethodNotAllowedException(POST_ERROR_MSG);
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve version contents as a file")
  @ApiResponses(value = {@ApiResponse(code = 200, response = String.class, message = "OK")})
  public Response getVersionContents(
      @ApiParam(allowableValues = "gzip")
      @HeaderParam(ACCEPT_ENCODING)
      String acceptEncoding,
      @PathParam("versionId")
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @NotNull
      @Valid
      UUID versionId
  ) {
    final var contentType = contentsService.getVersionMimetype(versionId)
                                           .orElse(APPLICATION_OCTET_STREAM);

    log.debug("Get version contents: versionId={}", versionId);
    var contents = contentsService.getByVersionId(versionId);
    log.debug("Got version contents: {}", contents);

    return contentsHelper.asAttachment(contents, acceptEncoding)
                         .header(CONTENT_TYPE, contentType)
                         .build();
  }

  @PUT
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = PUT_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = PUT_ERROR_MSG)})
  public Response putVersionContentsIsNotAllowed() {
    throw new MethodNotAllowedException(PUT_ERROR_MSG);
  }

  @DELETE
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = DELETE_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = DELETE_ERROR_MSG)})
  public Response deleteVersionContentsIsNotAllowed() {
    throw new MethodNotAllowedException(DELETE_ERROR_MSG);
  }

}
