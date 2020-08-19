package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormPageParams;
import nl.knaw.huc.api.ResultVersion;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.service.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huc.helpers.Paginator.toResult;

@Api(tags = {"files", "versions"})
@Path("/rest/files/{fileId}/versions")
public class FileVersionsResource {

  private static final Logger log = LoggerFactory.getLogger(FileVersionsResource.class);

  private final VersionService versionService;
  private final Paginator paginator;

  public FileVersionsResource(
      VersionService versionService,
      Paginator paginator
  ) {
    this.versionService = versionService;
    this.paginator = paginator;
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation("Retrieve file versions, newest first")
  @ApiResponses({@ApiResponse(code = 200, response = ResultVersion.class, message = "OK")})
  public Response getVersions(
      @PathParam("fileId") @Valid UUID fileId,
      @BeanParam FormPageParams pageParams,
      @QueryParam("createdAfter") LocalDateTime createdAfter
  ) {
    log.debug("Get versions of file: fileId={}; pageParams={}; createdAfter={}", fileId, pageParams, createdAfter);
    var results = versionService
        .getAll(fileId, paginator.fromForm(pageParams), createdAfter);
    log.debug("Got versions of file: {}", results);
    return Response
        .ok()
        .entity(toResult(results, ResultVersion::new))
        .build();
  }

}
