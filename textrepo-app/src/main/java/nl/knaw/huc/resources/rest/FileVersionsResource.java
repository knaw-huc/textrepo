package nl.knaw.huc.resources.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormPageParams;
import nl.knaw.huc.api.ResultVersion;
import nl.knaw.huc.service.Paginator;
import nl.knaw.huc.service.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huc.service.Paginator.toResult;

@Api(tags = {"files", "versions"})
@Path("/rest/files/{fileId}/versions")
public class FileVersionsResource {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private VersionService versionService;
  private Paginator paginator;

  public FileVersionsResource(VersionService versionService, Paginator paginator) {
    this.versionService = versionService;
    this.paginator = paginator;
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  @ApiOperation("Retrieve file versions")
  @ApiResponses({@ApiResponse(code = 200, responseContainer = "Map", response = ResultVersion.class, message = "OK")})
  public Response getVersions(
      @PathParam("fileId") @Valid UUID fileId,
      @BeanParam FormPageParams pageParams
  ) {
    logger.debug("get versions of file: fileId={}", fileId);
    var results = versionService
        .getAll(fileId, paginator.fromForm(pageParams));
    return Response
        .ok()
        .entity(toResult(results, ResultVersion::new))
        .build();
  }

}
