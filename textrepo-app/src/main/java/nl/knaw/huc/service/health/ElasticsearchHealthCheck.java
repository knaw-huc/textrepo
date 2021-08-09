package nl.knaw.huc.service.health;

import com.codahale.metrics.health.HealthCheck;
import nl.knaw.huc.service.index.EsIndexClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;

import java.io.IOException;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static com.codahale.metrics.health.HealthCheck.Result.unhealthy;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.elasticsearch.cluster.health.ClusterHealthStatus.GREEN;
import static org.elasticsearch.cluster.health.ClusterHealthStatus.YELLOW;

public class ElasticsearchHealthCheck extends HealthCheck {

  private final EsIndexClient client;

  public ElasticsearchHealthCheck(EsIndexClient client) {
    this.client = client;
  }

  @Override
  protected Result check() {

    ClusterHealthStatus status;
    try {
      status = client.getHealthStatus();
    } catch (IOException ex) {
      return unhealthy(format(
          "Health status: unknown; reason: %s: %s",
          ex.getClass().getName(), ex.getMessage()
      ));
    }
    var allowed = asList(GREEN, YELLOW);
    if (allowed.contains(status)) {
      return healthy("Health status: " + status);
    }
    return unhealthy("Health status: " + status + "; reason: " + status);
  }
}
