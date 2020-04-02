package nl.knaw.huc.textrepo;

import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST_ADMIN;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;

public class TestHealthChecks extends AbstractConcordionTest {

  public static class HealthCheckResult {
    public int status;
    public String postgres;
    public String deadlocks;
    public String index;
    public String indexer;
    public String body;
  }

  public HealthCheckResult checkHealth(String endpoint, String adminport) {
    final var response = client
        .target(HTTP_APP_HOST_ADMIN + endpoint)
        .request()
        .get();

    var result = new HealthCheckResult();

    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    result.postgres = json.read("$.postgresql.healthy", Boolean.class) ? "healthy" : "unhealthy";
    result.deadlocks = json.read("$.deadlocks.healthy", Boolean.class) ? "healthy" : "unhealthy";
    result.index = json.read("$.full-text-es-index.healthy", Boolean.class) ? "healthy" : "unhealthy";
    result.indexer = json.read("$.full-text-indexer-service.healthy", Boolean.class) ? "healthy" : "unhealthy";

    return result;
  }

}
