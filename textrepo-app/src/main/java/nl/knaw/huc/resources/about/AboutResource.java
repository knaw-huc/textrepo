package nl.knaw.huc.resources.about;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import nl.knaw.huc.api.ResultAbout;
import nl.knaw.huc.config.TextRepoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = {"about"})
@Path("/")
public class AboutResource {

  private static final Logger log = LoggerFactory.getLogger(AboutResource.class);

  private final TextRepoConfiguration config;

  public AboutResource(TextRepoConfiguration config) {
    this.config = requireNonNull(config);
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation("Get info about application version and configuration")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultAbout.class, message = "OK")})
  public ResultAbout getAbout(@HeaderParam("Authorization") String auth) {
    log.debug("Get documents overview, auth=[{}]", auth);
    return new ResultAbout(config);
  }

}
