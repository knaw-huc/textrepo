package nl.knaw.huc.resources.dashboard;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.UriBuilder.fromResource;
import static nl.knaw.huc.helpers.Paginator.toResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Map;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import nl.knaw.huc.api.FormPageParams;
import nl.knaw.huc.api.ResultDocument;
import nl.knaw.huc.api.ResultDocumentsOverview;
import nl.knaw.huc.api.ResultPage;
import nl.knaw.huc.helpers.Paginator;
import nl.knaw.huc.resources.rest.MetadataResource;
import nl.knaw.huc.service.dashboard.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = {"dashboard"})
@Path("/dashboard")
public class DashboardResource {

  private static final Logger log = LoggerFactory.getLogger(DashboardResource.class);

  private static final UriBuilder DOCUMENT_METADATA =
      fromResource(MetadataResource.class)
          .path("{key}")
          .path("documents");

  private final DashboardService dashboardService;
  private final Paginator paginator;

  public DashboardResource(DashboardService dashboardService, Paginator paginator) {
    this.dashboardService = requireNonNull(dashboardService);
    this.paginator = requireNonNull(paginator);
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation("Get document count overview")
  @ApiResponses(value = {
      @ApiResponse(code = 200, response = ResultDocumentsOverview.class, message = "OK")})
  public ResultDocumentsOverview getStats() {
    log.debug("Get documents overview");
    return new ResultDocumentsOverview(dashboardService.getDocumentsOverview());
  }

  @GET
  @Path("/orphans")
  @Produces(APPLICATION_JSON)
  @ApiOperation("Find orphans: documents with neither metadata nor any associated files")
  @ApiResponses(value = {@ApiResponse(code = 200, response = ResultPage.class, message = "OK")})
  public ResultPage<ResultDocument> findOrphans(
      @BeanParam
      FormPageParams pageParams
  ) {
    log.debug("Find orphans, pageParams={}", pageParams);
    var orphans = dashboardService.findOrphans(paginator.fromForm(pageParams));
    log.debug("Got orphans: {}", orphans);
    return toResult(orphans, ResultDocument::new);
  }

  @GET
  @Path("metadata")
  @ApiOperation("Get document count breakdown by metadata key (yields Map: key -> count)")
  @ApiResponses(@ApiResponse(code = 200, message = "OK. Body contains Map: String key -> int "
      + "count"))
  @Produces(APPLICATION_JSON)
  public Map<String, Integer> countDocumentsByMetadataKey() {
    log.debug("Count documents by metadata key");
    final var keyCounts = dashboardService.countDocumentsByMetadataKey();
    log.debug("Got keyCounts: {}", keyCounts);
    return keyCounts;
  }

  @GET
  @Path("metadata/{key}")
  @ApiOperation("Get document count breakdown by metadata value for given metadata key")
  @ApiResponses(@ApiResponse(
      code = 200,
      message = "OK. Body contains Map: String value -> int count. Link rel='collection' to "
          + "relevant documents"))
  @Produces(APPLICATION_JSON)
  public Response countDocumentsByMetadataValue(
      @PathParam("key")
      String key
  ) {
    log.debug("Count documents by metadata value for key=[{}]", key);
    final var valueCounts = dashboardService.countDocumentsByMetadataValue(key);
    log.debug("Got valueCounts: {}", valueCounts);
    return Response
        .ok(valueCounts)
        .link(DOCUMENT_METADATA.build(key), "collection")
        .build();
  }
}
