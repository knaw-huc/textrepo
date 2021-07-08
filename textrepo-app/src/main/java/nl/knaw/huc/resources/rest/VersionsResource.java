package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormVersion;
import nl.knaw.huc.api.ResultVersion;
import nl.knaw.huc.exceptions.MethodNotAllowedException;
import nl.knaw.huc.service.version.VersionService;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static nl.knaw.huc.resources.HeaderLink.Uri.FILE_VERSIONS;
import static nl.knaw.huc.resources.ResourceUtils.readContents;

@Api(tags = {"versions"})
@Path("/rest/versions")
public class VersionsResource {

  private static final Logger log = LoggerFactory.getLogger(VersionsResource.class);
  private static final String PUT_ERROR_MSG = "Not allowed to update a version: create a new version using POST";

  private final VersionService versionService;

  public VersionsResource(VersionService versionService) {
    this.versionService = requireNonNull(versionService);
  }

  @POST
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create version")
  @ApiResponses(value = {@ApiResponse(code = 201, response = ResultVersion.class, message = "Created")})
  public Response createVersion(
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @FormDataParam("fileId")
          UUID fileId,
      @ApiParam(required = true, example = "Hello world!")
      @FormDataParam("contents")
          InputStream inputStream
  ) {
    log.debug("Create version: fileId={}", fileId);
    var contents = readContents(inputStream);
    var version = versionService.createNewVersion(fileId, contents);
    log.debug("Created version: {}", version);
    return Response
        .created(FILE_VERSIONS.build(version.getId()))
        .entity(new ResultVersion(version))
        .build();
  }

  @GET
  @Path("/{id}")
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieve version")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultVersion.class, message = "OK")})
  public Response getVersion(
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @PathParam("id")
      @NotNull
      @Valid
          UUID id
  ) {
    log.debug("Get version: id={}", id);
    var version = versionService.get(id);
    log.debug("Got version: {}", version);
    return Response.ok(new ResultVersion(version)).build();
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = PUT_ERROR_MSG)
  @ApiResponses(value = {@ApiResponse(code = 405, message = PUT_ERROR_MSG)})
  public Response putVersionIsNotAllowed(
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @PathParam("id")
      @Valid
          UUID id,
      @ApiParam(required = true, example = "Hello world!")
      @Valid
          FormVersion form
  ) {
    throw new MethodNotAllowedException(PUT_ERROR_MSG);
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Delete version")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response deleteVersion(
      @ApiParam(required = true, example = "34739357-eb75-449b-b2df-d3f6289470d6")
      @PathParam("id")
      @Valid
          UUID id
  ) {
    log.debug("Delete version: id={}", id);
    versionService.delete(id);
    log.debug("Deleted version");
    return Response.ok().build();
  }

}
