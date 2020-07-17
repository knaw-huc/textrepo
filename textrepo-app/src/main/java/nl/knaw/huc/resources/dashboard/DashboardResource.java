package nl.knaw.huc.resources.dashboard;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.knaw.huc.api.FormPageParams;
import nl.knaw.huc.api.ResultDocument;
import nl.knaw.huc.api.ResultDocumentsOverview;
import nl.knaw.huc.api.ResultPage;
import nl.knaw.huc.db.DashboardDao.KeyCount;
import nl.knaw.huc.service.DashboardService;
import nl.knaw.huc.service.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huc.service.Paginator.toResult;

@Api(tags = {"dashboard"})
@Path("/dashboard")
public class DashboardResource {

  private static final Logger log = LoggerFactory.getLogger(DashboardResource.class);
  private final DashboardService dashboardService;
  private final Paginator paginator;

  public DashboardResource(DashboardService dashboardService, Paginator paginator) {
    this.dashboardService = dashboardService;
    this.paginator = paginator;
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation("Get document count overview")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocumentsOverview.class, message = "OK")})
  public ResultDocumentsOverview getStats() {
    log.debug("Get documents overview");
    return new ResultDocumentsOverview(dashboardService.getDocumentsOverview());
  }

  @GET
  @Path("/orphans")
  @Produces(APPLICATION_JSON)
  @ApiOperation("Find orphans: documents with neither metadata nor any associated files")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultDocument.class, message = "OK")})
  public ResultPage<ResultDocument> findOrphans(@BeanParam FormPageParams pageParams) {
    log.debug("Find orphans");
    var orphans = dashboardService.findOrphans(paginator.fromForm(pageParams));
    log.debug("Got orphans: {}", orphans);
    return toResult(orphans, ResultDocument::new);
  }

  @GET
  @Path("metadata")
  @Produces(APPLICATION_JSON)
  public List<KeyCount> documentCountsByMetadataKey() {
    log.debug("Get document breakdown by metadata key");
    final var keyCounts = dashboardService.documentCountsByMetadataKey();
    log.debug("Got keycounts: {}", keyCounts);
    return keyCounts;
  }
}
