package nl.knaw.huc.resources.dashboard;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.ResultDocument;
import nl.knaw.huc.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.valueOf;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"dashboard"})
@Path("/dashboard")
public class DashboardResource {

  private static final Logger log = LoggerFactory.getLogger(DashboardResource.class);
  private final DashboardService dashboardService;

  public DashboardResource(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get dashboard statistics")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public Map<String, String> getStats() {
    log.debug("Get dashboard statistics");
    final var stats = new HashMap<String, String>();
    stats.put("documentCount", valueOf(dashboardService.countDocuments()));
    stats.put("documentsWithoutFiles", valueOf(dashboardService.countDocumentsWithoutFiles()));
    stats.put("documentsWithoutMetadata", valueOf(dashboardService.countDocumentsWithoutMetadata()));
    stats.put("orphans", valueOf(dashboardService.countOrphans()));
    return stats;
  }
}
