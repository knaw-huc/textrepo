package nl.knaw.huc.service.health;

import com.codahale.metrics.health.HealthCheck;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import static java.lang.String.format;

public class IndexerHealthCheck extends HealthCheck {

  private final String mapping;
  private final JerseyClient client;

  public IndexerHealthCheck(String mapping) {
    this.client = JerseyClientBuilder.createClient();
    this.mapping = mapping;
  }

  @Override
  protected Result check() {
    var response = this.client.target(this.mapping).request().get();
    if (response.getStatus() == 200) {
      return Result.healthy("Mapping endpoint is up");
    }
    return Result.unhealthy(format("Mapping endpoint returned status [%d]", response.getStatus()));
  }

}
