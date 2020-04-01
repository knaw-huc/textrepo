package nl.knaw.huc.service.health;

import com.codahale.metrics.health.HealthCheck;
import nl.knaw.huc.service.index.TextRepoElasticClient;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.cluster.health.ClusterHealthStatus.GREEN;
import static org.elasticsearch.cluster.health.ClusterHealthStatus.YELLOW;

public class ElasticsearchHealthCheck extends HealthCheck {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final String indexName;
  private final TextRepoElasticClient client;

  public ElasticsearchHealthCheck(String indexName, TextRepoElasticClient client) {
    this.indexName = indexName;
    this.client = client;
  }

  @Override
  protected Result check() {
    var request = new ClusterHealthRequest(indexName);

    ClusterHealthResponse response;
    try {
      response = client
          .getClient()
          .cluster()
          .health(request, DEFAULT);
    } catch (IOException ex) {
      return HealthCheck.Result.unhealthy(format(
          "Health status: unknown; reason: %s: %s",
          ex.getClass().getName(), ex.getMessage()
      ));
    }

    var allowed = asList(GREEN, YELLOW);
    if (allowed.contains(response.getStatus())) {
      return HealthCheck.Result.healthy("Health status: " + response.getStatus());
    }
    return HealthCheck.Result.unhealthy("Health status: " + response.getStatus() + "; reason: " + response.status());
  }
}
